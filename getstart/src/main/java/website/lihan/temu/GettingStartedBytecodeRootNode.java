package website.lihan.temu;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.bytecode.BytecodeRootNode;
import com.oracle.truffle.api.bytecode.Operation;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.nodes.RootNode;

public abstract class GettingStartedBytecodeRootNode extends RootNode implements BytecodeRootNode {

  /**
   * All Bytecode root nodes must define a constructor that takes only a {@link TruffleLanguage} and
   * a {@link FrameDescriptor} (or {@link FrameDescriptor.Builder}).
   */
  protected GettingStartedBytecodeRootNode(
      BytecodeDSLTestLanguage language, FrameDescriptor frameDescriptor) {
    super(language, frameDescriptor);
  }

  /**
   * Bytecode root nodes can define fields. Because the constructor cannot take additional
   * parameters, these fields must be initialized at a later time (consider annotations like {@link
   * CompilationFinal} if the field is effectively final).
   */
  @CompilationFinal String name;

  public void setName(String name) {
    CompilerDirectives.transferToInterpreterAndInvalidate();
    this.name = name;
  }

  /**
   * Operations can be defined inside the bytecode root node class. They declare their semantics in
   * much the same way as Truffle DSL nodes, with some additional restrictions (see {@link
   * Operation}).
   */
  @Operation
  public static final class Add {
    @Specialization
    public static int doInts(int a, int b) {
      return a + b;
    }
  }

  @Operation
  public static final class Div {
    @Specialization
    public static int doInts(int a, int b) {
      return a / b;
    }
  }

  @Operation
  public static final class Equals {
    @Specialization
    public static boolean doInts(int a, int b) {
      return a == b;
    }
  }

  @Operation
  public static final class LessThan {
    @Specialization
    public static boolean doInts(int a, int b) {
      return a < b;
    }
  }

  /**
   * This is an eager OR operation. It does not use the Bytecode DSL's short-circuiting
   * capabilities.
   */
  @Operation
  public static final class EagerOr {
    @Specialization
    public static boolean doBools(boolean a, boolean b) {
      return a | b;
    }
  }

  /**
   * This class is used as a boolean converter for the short-circuit {@code ScOr} operation defined
   * above. There are some additional restrictions on boolean converters, namely that they must take
   * a single argument and they must return boolean.
   */
  @Operation
  public static final class ToBool {
    @Specialization
    public static boolean doBool(boolean b) {
      return b;
    }

    @Specialization
    public static boolean doInt(int i) {
      return i != 0;
    }
  }

  /** These operations are used in {@link ParsingTutorial}. You can ignore them for now. */
  @Operation
  public static final class ArrayLength {
    @Specialization
    public static int doInt(int[] array) {
      return array.length;
    }
  }

  @Operation
  public static final class ArrayIndex {
    @Specialization
    public static int doInt(int[] array, int index) {
      return array[index];
    }
  }
}
