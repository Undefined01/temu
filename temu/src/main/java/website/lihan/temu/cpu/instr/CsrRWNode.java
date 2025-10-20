package website.lihan.temu.cpu.instr;

import static website.lihan.temu.cpu.RvUtils.BYTES;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;

import website.lihan.temu.Rv64Context;
import website.lihan.temu.cpu.IllegalInstructionException;
import website.lihan.temu.cpu.csr.CsrLibrary;
import website.lihan.temu.device.Bus;
import website.lihan.temu.device.DeviceLibrary;

public class CsrRWNode extends Node {
  public final int csrId;
  public final int rs1;
  public final int rd;
  public final int funct3;

  private final Object csr;
  @Child
  private CsrLibrary csrLib;

  public CsrRWNode(int csrId, int rs1, int rd, int funct3) {
    this.csrId = csrId;
    this.rs1 = rs1;
    this.rd = rd;
    this.funct3 = funct3;

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
