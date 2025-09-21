package website.lihan.temu.cpu;

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
    return bytecodeNode.execute(frame);
  }
}
