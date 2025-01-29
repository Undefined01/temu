package website.lihan.temu.cpu;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;

import website.lihan.temu.Rv64BytecodeLanguage;

public class Rv64BytecodeRootNode extends RootNode {
  @Child Rv64BytecodeNode cpu;

  public Rv64BytecodeRootNode(Rv64BytecodeLanguage language, Rv64BytecodeNode cpu) {
    super(language);
    this.cpu = cpu;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    return cpu.execute(frame);
  }
}
