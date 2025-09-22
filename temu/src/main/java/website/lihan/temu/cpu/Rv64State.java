package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

public final class Rv64State {
  @CompilationFinal(dimensions = 0)
  private final long[] regs = new long[32];

  @CompilationFinal(dimensions = 0)
  private final long[] csrs = new long[CSR.COUNT];

  public long pc = 0x80000000L;

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

  public long readCSR(int csr) {
    return csrs[CSR.getIndex(csr)];
  }

  public void writeCSR(int csr, long value) {
    csrs[CSR.getIndex(csr)] = value;
  }

  public static class CSR {
    public static final int SSTATUS = 0x100;
    public static final int SIE = 0x104;
    public static final int STVEC = 0x105;
    public static final int SSCRATCH = 0x140;
    public static final int SEPC = 0x141;
    public static final int SCAUSE = 0x142;
    public static final int STVAL = 0x143;
    public static final int COUNT = 7;

    public static int getIndex(int csr) {
      return switch (csr) {
        case SSTATUS -> 0;
        case SIE -> 1;
        case STVEC -> 2;
        case SSCRATCH -> 3;
        case SEPC -> 4;
        case SCAUSE -> 5;
        case STVAL -> 6;
        default -> throw IllegalInstructionException.create("Unknown CSR: %04x", csr);
      };
    }
  }
}
