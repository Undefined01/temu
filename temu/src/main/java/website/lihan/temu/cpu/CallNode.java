package website.lihan.temu.cpu;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;

public class CallNode extends Node {
  public final long targetPc;
  public final long returnPc;

  @CompilationFinal private CallTarget target;
  @Child private DirectCallNode directCallNode;

  public CallNode(long targetPc, long returnPc) {
    this.targetPc = targetPc;
    this.returnPc = returnPc;
    this.target = null;
    this.directCallNode = null;
  }

  public void call(Rv64State cpu) {
    if (directCallNode == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      var context = Rv64Context.get(this);
      var targetNode = context.getExecPageCache().getByEntryPoint(targetPc);
      this.target = targetNode.getCallTarget();
      this.directCallNode = DirectCallNode.create(target);
    }
    cpu.setReg(1, returnPc); // ra
    directCallNode.call(cpu, returnPc);
  }
}
