package website.lihan.temu.processor;

import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import website.lihan.temu.annotations.*;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("website.lihan.temu.annotations.RiscvCsr")
public class BitFieldProcessor extends AbstractProcessor {

  private Filer filer;
  private Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.filer = processingEnv.getFiler();
    this.messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // Find all classes annotated with @RiscvCsr
    for (Element element : roundEnv.getElementsAnnotatedWith(RiscvCsr.class)) {
      if (element.getKind() != ElementKind.CLASS) {
        error("Only classes can be annotated with @RiscvCsr", element);
        return true;
      }

      TypeElement classElement = (TypeElement) element;
      try {
        generateBitFieldClass(classElement);
      } catch (IOException e) {
        error("Could not create generated class: " + e.getMessage(), classElement);
      }
    }
    return true;
  }

  private void generateBitFieldClass(TypeElement classElement) throws IOException {
    String originalClassName = classElement.getSimpleName().toString();
    String packageName =
        processingEnv.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
    String generatedClassName;
    if (originalClassName.endsWith("Def")) {
      generatedClassName = originalClassName.substring(0, originalClassName.length() - 3);
    } else {
      generatedClassName = originalClassName + "Gen";
    }

    JavaFileObject file =
        filer.createSourceFile(packageName + "." + generatedClassName, classElement);

    List<BitFieldInfo> fieldInfos = new ArrayList<>();
    long trivialMask = 0L;
    long wpriMask = 0L;
    long warlMask = 0L;
    long implementedMask = 0L;
    long resetValue = classElement.getAnnotation(RiscvCsr.class).resetValue();

    // First pass: collect info and build aggregate masks
    for (Element enclosed : classElement.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.FIELD) {
        BitField bitField = enclosed.getAnnotation(BitField.class);
        if (bitField == null) {
          continue; // Skip fields without @BitField
        }

        BitFieldInfo info = new BitFieldInfo(enclosed, bitField);
        fieldInfos.add(info);
        implementedMask |= info.mask;

        if ((info.resetValue & info.mask) != 0) {
          error("Reset value out of range for field " + info.fieldName, enclosed);
        }
        resetValue |= (info.resetValue << info.offset) & info.mask;

        if (enclosed.getAnnotation(Wpri.class) != null) {
          info.behavior = Behavior.WPRI;
          wpriMask |= info.mask;
        } else if (enclosed.getAnnotation(Warl.class) != null) {
          info.behavior = Behavior.WARL;
          warlMask |= info.mask;
          info.warlValues = enclosed.getAnnotation(Warl.class).values();
        } else {
          info.behavior = Behavior.TRIVIAL;
          trivialMask |= info.mask;
        }
      }
    }

    wpriMask |= ~implementedMask;

    // Now, write the file
    try (PrintWriter writer = new PrintWriter(file.openWriter())) {
      writer.println("package " + packageName + ";");
      writer.println();
      writer.println("import website.lihan.temu.Utils;");
      writer.println("import javax.annotation.processing.Generated;");
      writer.println("import com.oracle.truffle.api.library.ExportLibrary;");
      writer.println("import com.oracle.truffle.api.library.ExportMessage;");
      writer.println();
      writer.println("@Generated(\"" + BitFieldProcessor.class.getCanonicalName() + "\")");
      writer.println("@ExportLibrary(CsrLibrary.class)");
      writer.println("public class " + generatedClassName + " {");
      writer.println();
      writer.println("    protected long value;");
      writer.println();

      // --- Define Constants ---
      for (BitFieldInfo info : fieldInfos) {
        writer.printf("    public static final int %s_OFFSET = %d;%n", info.constName, info.offset);
        writer.printf("    public static final int %s_BITS = %d;%n", info.constName, info.length);
        writer.printf("    public static final long %s_MASK = 0x%xL;%n", info.constName, info.mask);
      }
      writer.printf("    private static final long TRIVIAL_FIELDS_MASK = 0x%xL;%n", trivialMask);
      writer.printf("    private static final long WPRI_FIELDS_MASK = 0x%xL;%n", wpriMask);
      writer.printf("    private static final long WARL_FIELDS_MASK = 0x%xL;%n", warlMask);
      writer.println();

      // --- Constructor ---
      writer.println("    public " + generatedClassName + "() {");
      writer.printf("        this(0x%xL);%n", resetValue);
      writer.println("    }");
      writer.println();
      writer.println("    public " + generatedClassName + "(long initialValue) {");
      writer.println("        this.value = initialValue;");
      writer.println("    }");
      writer.println();

      // --- Getters and Setters ---
      for (BitFieldInfo info : fieldInfos) {
        generateGetter(writer, info);
        generateSetter(writer, info);
      }

      // --- getValue() ---
      writer.println("    @ExportMessage");
      writer.println("    public long getValue() {");
      writer.println("        return this.value;");
      writer.println("    }");
      writer.println();

      // --- setValue() ---
      writer.println("    @ExportMessage");
      writer.println("    public void setValue(long newValue) {");
      writer.println("        long diff = this.value ^ newValue;");
      writer.println();
      writer.println("        // Handle WPRI fields: Write Preserve, Read Ignored");
      writer.println("        var wpriDiff = diff & WPRI_FIELDS_MASK;");
      writer.println("        if (wpriDiff != 0) {");
      writer.println(
          "            Utils.printf(\"Ignoring write to WPRI fields %016x in "
              + originalClassName
              + "\\n\", wpriDiff);");
      writer.println("            diff ^= wpriDiff; // Clear diff bits for WPRI fields");
      writer.println(
          "            newValue ^= wpriDiff; // Preserve old value");
      writer.println("        }");
      writer.println();
      writer.println("        // Handle Trivial fields: Simple R/W");
      writer.println(
          "        // Combine old value (keeping non-trivial) with new value (trivial only)");
      writer.println("        this.value ^= (diff & TRIVIAL_FIELDS_MASK);");
      writer.println();
      writer.println("        // Handle WARL fields: Call individual setters for validation");
      for (BitFieldInfo info : fieldInfos) {
        if (info.behavior == Behavior.WARL) {
          writer.printf("        if ((diff & %s_MASK) != 0) {%n", info.constName);
          writer.printf(
              "            set%s((%s)((newValue & %s_MASK) >> %s_OFFSET));%n",
              info.capitalizedName, info.typeName, info.constName, info.constName);
          writer.println("        }");
        }
      }
      writer.println("    }");
      writer.println();

      writer.println("}"); // End of class
    }
  }

  private void generateGetter(PrintWriter writer, BitFieldInfo info) {
    writer.printf("    public %s get%s() {%n", info.typeName, info.capitalizedName);
    if (info.isBoolean) {
      writer.printf("        return (value & %s_MASK) != 0;%n", info.constName);
    } else {
      writer.printf(
          "        return (%s)((value & %s_MASK) >> %s_OFFSET);%n",
          info.typeName, info.constName, info.constName);
    }
    writer.println("    }");
    writer.println();
  }

  private void generateSetter(PrintWriter writer, BitFieldInfo info) {
    // We don't generate individual setters for WPRI fields
    if (info.behavior == Behavior.WPRI) {
      return;
    }

    writer.printf(
        "    public void set%s(%s %s) {%n", info.capitalizedName, info.typeName, info.fieldName);

    if (info.behavior == Behavior.WARL) {
      writer.println("        // Write Any Read Legal: check against allowed values");
      String check =
          Arrays.stream(info.warlValues)
              .mapToObj(val -> "(" + info.fieldName + " != " + val + ")")
              .collect(Collectors.joining(" && "));
      writer.println("        if (" + check + ") {");
      writer.println(
          "            Utils.printf(\"Ignoring illegal write value %d for field "
              + info.fieldName
              + " in "
              + info.enclosingClass
              + "\\n\", "
              + info.fieldName
              + ");");
      writer.println("            return;");
      writer.println("        }");
    }

    if (info.isBoolean) {
      writer.printf("        if (%s) {%n", info.fieldName);
      writer.printf("            value |= %s_MASK;%n", info.constName);
      writer.println("        } else {");
      writer.printf("            value &= ~%s_MASK;%n", info.constName);
      writer.println("        }");
    } else {
      writer.printf(
          "        value = (value & ~%s_MASK) | (((long)%s << %s_OFFSET) & %s_MASK);%n",
          info.constName, info.fieldName, info.constName, info.constName);
    }
    writer.println("    }");
    writer.println();
  }

  // --- Helper enum and class ---

  private enum Behavior {
    TRIVIAL,
    WARL,
    WPRI
  }

  private static class BitFieldInfo {
    String fieldName;
    String capitalizedName;
    String constName;
    String typeName;
    String enclosingClass;
    boolean isBoolean;
    int offset;
    int length;
    long mask;
    long resetValue;
    Behavior behavior;
    long[] warlValues;

    BitFieldInfo(Element fieldElement, BitField bitField) {
      this.fieldName = fieldElement.getSimpleName().toString();
      this.capitalizedName = capitalize(fieldName);
      this.constName = fieldName.toUpperCase();
      this.enclosingClass = fieldElement.getEnclosingElement().getSimpleName().toString();

      TypeMirror typeMirror = fieldElement.asType();
      this.typeName = typeMirror.toString();
      this.isBoolean = this.typeName.equals("boolean");

      this.offset = bitField.offset();
      this.length = bitField.length();

      // Calculate mask: (2^length - 1) << offset
      long rawMask = (1L << length) - 1;
      this.mask = rawMask << offset;

      this.resetValue = bitField.resetValue();
    }

    private static String capitalize(String s) {
      if (s == null || s.isEmpty()) {
        return s;
      }
      // return s.substring(0, 1).toUpperCase() + s.substring(1);
      return s.toUpperCase();
    }
  }

  // --- Error Logging ---

  private void error(String msg, Element e) {
    messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
  }

  private void warn(String msg, Element e) {
    messager.printMessage(Diagnostic.Kind.WARNING, msg, e);
  }
}
