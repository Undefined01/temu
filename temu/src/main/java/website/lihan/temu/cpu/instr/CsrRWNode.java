package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.cpu.IllegalInstructionException;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.RvUtils.IInstruct;
import website.lihan.temu.cpu.csr.CsrLibrary;

public final class CsrRWNode extends Node {
  public final long pc;
  public final int csrId;
  public final int csrPrivLevel;
  public final int csrRWPermission;
  public final int rs1;
  public final int rd;
  public final int funct3;

  private final Object csr;
  @Child private CsrLibrary csrLib;

  public CsrRWNode(IInstruct i, long pc) {
    this.pc = pc;
    this.csrId = i.imm() & 0xfff;
    this.csrPrivLevel = (csrId & 0x300) >> 8;
    this.csrRWPermission = (csrId & 0xc00) >> 10;
    this.rs1 = i.rs1();
    this.rd = i.rd();
    this.funct3 = i.funct3();

    var context = Rv64Context.get(this);
    this.csr = context.getState().getCsrById(csrId);
    if (csr != null) {
      this.csrLib = CsrLibrary.getFactory().create(csr);
    }
  }

  public void checkPrivilege(Rv64State cpu) {
    if (cpu.getPrivilegeLevel().level() < csrPrivLevel) {
      throw IllegalInstructionException.create(
          pc, "Cannot access CSR %03x in priviledge level %d", cpu.getPrivilegeLevel().level());
    }
  }

  public long read() {
    if (csr == null) {
      throw IllegalInstructionException.create(pc, "CSR %03x is unimplemented", csrId);
    }
    return csrLib.getValue(csr);
  }

  public void write(long newValue) {
    if (csr == null) {
      throw IllegalInstructionException.create(pc, "CSR %03x is unimplemented", csrId);
    }
    if (csrRWPermission == 0b11) {
      throw IllegalInstructionException.create(pc, "Cannot write to read-only CSR %03x", csrId);
    }
    csrLib.setValue(csr, newValue);
  }
}
