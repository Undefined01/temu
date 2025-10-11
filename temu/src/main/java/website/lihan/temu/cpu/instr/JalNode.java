package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.nodes.Node;

public class JalNode extends Node {
  public final long targetPc;
  public final long returnPc;
  public final int rd;

  public JalNode(long targetPc, long returnPc, int rd) {
    this.targetPc = targetPc;
    this.returnPc = returnPc;
    this.rd = rd;
  }
}
