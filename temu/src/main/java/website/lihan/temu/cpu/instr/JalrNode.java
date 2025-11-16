package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.cpu.ExecPageCache;
import website.lihan.temu.cpu.Rv64State;

public abstract class JalrNode extends Node {
  public final int rs1;
  public final int imm;
  public final int rd;
  public final long returnPc;
  private final Rv64Context context;
  private final ExecPageCache pageCache;

  public JalrNode(int rs1, int imm, int rd, long returnPc) {
    this.rs1 = rs1;
    this.imm = imm;
    this.rd = rd;
    this.returnPc = returnPc;
    this.context = Rv64Context.get(this);
    this.pageCache = context.getExecPageCache();
  }

  public abstract void execute(Rv64State cpu);

  @Specialization(
      guards = {"getTargetPc(cpu) == cachedTargetPc"},
      limit = "2")
  void doDirect(
      Rv64State cpu,
      @Cached("getTargetPc(cpu)") long cachedTargetPc,
      @Cached("getCallTarget(cpu, cachedTargetPc)") CallTarget cachedCallTarget,
      @Cached("create(cachedCallTarget)") DirectCallNode directCallNode) {
    cpu.setReg(rd, returnPc);
    directCallNode.call(0, returnPc);
  }

  @Specialization(replaces = "doDirect")
  void doIndirect(Rv64State cpu, @Cached IndirectCallNode indirectCallNode) {
    cpu.setReg(rd, returnPc);
    var callTarget = getCallTarget(cpu, getTargetPc(cpu));
    indirectCallNode.call(callTarget, 0, returnPc);
  }

  long getTargetPc(Rv64State cpu) {
    return cpu.getReg(rs1) + imm;
  }

  CallTarget getCallTarget(Rv64State cpu, long targetPc) {
    return pageCache.getByEntryPoint(cpu, targetPc).getCallTarget();
  }
}
