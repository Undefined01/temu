package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

import website.lihan.temu.Utils;
import website.lihan.temu.cpu.csr.CsrFile;
import website.lihan.temu.cpu.csr.CsrLibrary;

public final class Rv64State {
  @CompilationFinal(dimensions = 0)
  private final long[] regs = new long[32];

  private final CsrFile csrs = new CsrFile();

  // public long pc = 0x80000000L;
  @CompilationFinal
  private int privilegeLevel = 1;

  public long getReg(int reg) {
    if (reg == 0) {
      return 0;
    }
    return regs[reg];
  }

  public void setReg(int reg, long value) {
    if (reg != 0) {
      regs[reg] = value;
    }
  }

  public Object getCsrById(int csrId) {
    return csrs.getCsrById(csrId);
  }

  public CsrFile getCsrFile() {
    return csrs;
  }

  public int getPrivilegeLevel() {
    return privilegeLevel;
  }

  public boolean isInterruptEnabled() {
    switch (privilegeLevel) {
      case 1:
        return csrs.mstatus.getSIE();
      case 3:
        return csrs.mstatus.getMIE();
      default:
        throw CompilerDirectives.shouldNotReachHere();
    }
  }
}
