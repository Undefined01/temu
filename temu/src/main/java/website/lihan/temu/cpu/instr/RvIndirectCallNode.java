package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.cpu.ExecPageCache;
import website.lihan.temu.cpu.Rv64State;

public abstract class RvIndirectCallNode extends Node {
  public final int rs1;
  public final int imm;
  public final int rd;
  public final long returnPc;
  private final Rv64Context context;

  public RvIndirectCallNode(int rs1, int imm, int rd, long returnPc) {
    this.rs1 = rs1;
    this.imm = imm;
    this.rd = rd;
    this.returnPc = returnPc;
    context = Rv64Context.get(this);
  }

  public abstract void execute(Rv64State cpu, long parentPc);

  @Specialization(
      guards = {"getTargetPc(cpu) == cachedTargetPc"},
      limit = "2")
  void doDirect(Rv64State cpu, long parentPc, @Cached("getTargetPc(cpu)") long cachedTargetPc, @Cached("getCallTarget(cpu)") CallTarget callTarget, @Cached("create(callTarget)") DirectCallNode directCallNode) {
    cpu.setReg(rd, returnPc); // ra
    directCallNode.call((int)(cachedTargetPc & ~ExecPageCache.PAGE_ADDR_MASK), parentPc);
  }

  @Specialization(replaces = "doDirect")
  void doIndirect(Rv64State cpu, long parentPc, @Cached IndirectCallNode indirectCallNode) {
    cpu.setReg(rd, returnPc); // ra
    var callTarget = getCallTarget(cpu);
    indirectCallNode.call(callTarget, (int)(getTargetPc(cpu) & ~ExecPageCache.PAGE_ADDR_MASK), parentPc);
  }

  long getTargetPc(Rv64State cpu) {
    return cpu.getReg(rs1) + imm;
  }

  CallTarget getCallTarget(Rv64State cpu) {
    return context.getExecPageCache().getByEntryPoint(getTargetPc(cpu), cpu).getCallTarget();
  }
}
