package website.lihan.temu.cpu;

import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import website.lihan.temu.Rv64BytecodeLanguage;

public class Rv64BytecodeRootNode extends RootNode {
  @Child Rv64BytecodeNode bytecodeNode;

  public Rv64BytecodeRootNode(Rv64BytecodeLanguage language, Rv64BytecodeNode bytecodeNode) {
    super(language);
    this.bytecodeNode = bytecodeNode;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    var bci = (int) frame.getArguments()[0];
    return bytecodeNode.execute(frame);
  }

  @Override
  public Map<String, Object> getDebugProperties() {
      Map<String, Object> properties = new HashMap<>();
      properties.put("baseAddr", String.format("[0x%08x, 0x%08x)", bytecodeNode.baseAddr, bytecodeNode.baseAddr + bytecodeNode.bc.length));
      properties.put("entryPoint", String.format("0x%08x", bytecodeNode.baseAddr + bytecodeNode.entryBci));
      return properties;
  }
}
