package website.lihan.temu;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.ContextPolicy;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.strings.TruffleString;
import website.lihan.temu.bus.Bus;
import website.lihan.temu.bus.Memory;
import website.lihan.temu.bus.RTC;
import website.lihan.temu.bus.SerialPort;
import website.lihan.temu.cpu.Rv64BytecodeNode;
import website.lihan.temu.cpu.Rv64BytecodeRootNode;

@TruffleLanguage.Registration(
    id = Rv64BytecodeLanguage.ID,
    name = "Riscv 64 Bytecode",
    contextPolicy = ContextPolicy.SHARED,
    defaultMimeType = "application/x-rv64-bytecode",
    byteMimeTypes = {"application/x-rv64-bytecode"})
public final class Rv64BytecodeLanguage extends TruffleLanguage<Rv64Context> {
  public static final String ID = "rv64";

  public static final TruffleString.Encoding STRING_ENCODING = TruffleString.Encoding.UTF_16;
  private static final LanguageReference<Rv64BytecodeLanguage> REF =
      LanguageReference.create(Rv64BytecodeLanguage.class);

  public final Shape attrsetShape = Shape.newBuilder().build();

  public static Rv64BytecodeLanguage get(Node node) {
    return REF.get(node);
  }

  @Override
  protected CallTarget parse(ParsingRequest request) throws Exception {
    var source = request.getSource();

    var memory = new Memory();
    var bytecode = source.getBytes().toByteArray();
    memory.write(0, bytecode, bytecode.length);
    var bus = new Bus(new Object[] {memory, new SerialPort(), new RTC()});
    // var evalRootNode = new Rv64ExecutionRootNode(this, bus);
    var bytecodeNode = new Rv64BytecodeNode(bus, 0x80000000L, bytecode, 0);
    var evalRootNode = new Rv64BytecodeRootNode(this, bytecodeNode);

    return evalRootNode.getCallTarget();
  }

  @Override
  protected Object getScope(Rv64Context context) {
    return context.createScopeObject();
  }

  @Override
  protected Rv64Context createContext(Env env) {
    var context = new Rv64Context(this, null);
    return context;
  }
}
