package website.lihan.temu.cpu;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import website.lihan.temu.Rv64BytecodeLanguage;
import website.lihan.temu.Utils;

public class Rv64BytecodeRootNode extends RootNode {
  @Child Rv64BytecodeNode bytecodeNode;

  public Rv64BytecodeRootNode(Rv64BytecodeLanguage language, Rv64BytecodeNode bytecodeNode) {
    super(language);
    this.bytecodeNode = bytecodeNode;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    var cpu = (Rv64State)frame.getArguments()[0];
    return bytecodeNode.execute(frame, cpu);
  }
}
