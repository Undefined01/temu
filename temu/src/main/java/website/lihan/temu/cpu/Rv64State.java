package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

public final class Rv64State {
  @CompilationFinal(dimensions = 1)
  private final long[] regs = new long[32];
  
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
}
