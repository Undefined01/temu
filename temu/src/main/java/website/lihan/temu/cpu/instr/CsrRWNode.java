package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.cpu.IllegalInstructionException;
import website.lihan.temu.cpu.RvUtils.IInstruct;
import website.lihan.temu.cpu.csr.CsrLibrary;

public class CsrRWNode extends Node {
  public final long pc;
  public final int csrId;
  public final int rs1;
  public final int rd;
  public final int funct3;

  private final Object csr;
  @Child private CsrLibrary csrLib;

  public CsrRWNode(IInstruct i, long pc) {
    this.pc = pc;
    this.csrId = i.imm() & 0xfff;
    this.rs1 = i.rs1();
    this.rd = i.rd();
    this.funct3 = i.funct3();

    var context = Rv64Context.get(this);
    this.csr = context.getState().getCsrById(csrId);
    if (csr != null) {
      this.csrLib = CsrLibrary.getFactory().create(csr);
    }
  }

  public long read() {
    if (csr == null) {
      throw IllegalInstructionException.create("CSR %03x not implemented", csrId);
    }
    return csrLib.getValue(csr);
  }

  public void write(long newValue) {
    if (csr == null) {
      throw IllegalInstructionException.create("CSR %03x not implemented", csrId);
    }
    csrLib.setValue(csr, newValue);
  }
}
