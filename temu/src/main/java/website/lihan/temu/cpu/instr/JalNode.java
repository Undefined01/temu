package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.cpu.RegId;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.RvUtils.JInstruct;

public final class JalNode extends Node {
  public final long targetPc;
  public final long returnPc;
  public final int rd;
  public final boolean isCall;

  @CompilationFinal private CallTarget target;
  @Child private DirectCallNode directCallNode;

  public JalNode(JInstruct j, long pc) {
    this.targetPc = pc + j.imm();
    this.returnPc = pc + 4;
    this.rd = j.rd();
    this.isCall = j.rd() == 1;
  }

  public void call(Rv64State cpu) {
    if (directCallNode == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      var context = Rv64Context.get(this);
      var targetNode = context.getExecPageCache().getByEntryPoint(cpu, targetPc);
      this.target = targetNode.getCallTarget();
      this.directCallNode = DirectCallNode.create(target);
    }
    cpu.setReg(RegId.ra, returnPc);
    directCallNode.call(0, returnPc);
  }

  public static boolean jalrIsReturn(VirtualFrame frame, long nextPc) {
    return frame.getArguments().length > 1 && (long) frame.getArguments()[1] == nextPc;
  }
}
