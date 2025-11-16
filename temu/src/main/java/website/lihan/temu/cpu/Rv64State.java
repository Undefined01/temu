package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import website.lihan.temu.cpu.csr.CsrFile;
import website.lihan.temu.device.RTC;

public final class Rv64State {
  @CompilationFinal(dimensions = 0)
  private long[] regs = new long[32];

  @CompilationFinal(dimensions = 0)
  private CsrFile csrs = new CsrFile();

  @CompilationFinal private PrivilegeLevel privilegeLevel;

  public Rv64State() {
    this(PrivilegeLevel.M);
  }

  public Rv64State(PrivilegeLevel privilegeLevel) {
    this.privilegeLevel = privilegeLevel;
  }

  @TruffleBoundary
  public Rv64State clone(PrivilegeLevel privilegeLevel) {
    var cloned = new Rv64State(privilegeLevel);
    cloned.regs = this.regs;
    cloned.csrs = this.csrs;
    return cloned;
  }

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

  public PrivilegeLevel getPrivilegeLevel() {
    return privilegeLevel;
  }

  public void setPrivilegeLevel(PrivilegeLevel privilegeLevel) {
    this.privilegeLevel = privilegeLevel;
  }

  public boolean isInterruptEnabled() {
    switch (privilegeLevel) {
      case U:
        return true;
      case S:
        return csrs.mstatus.getSIE();
      case M:
        return csrs.mstatus.getMIE();
      default:
        throw CompilerDirectives.shouldNotReachHere();
    }
  }

  public void throwPendingInterrupt(long pc) throws InterruptException {
    if (this.isInterruptEnabled() && RTC.checkInterrupt()) {
      throw InterruptException.create(pc, InterruptException.Cause.STIMER);
    }
  }
}
