package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ControlFlowException;

public class InterruptException extends ControlFlowException {
  public final long pc;
  public final long cause;
  public final long stval;

  public static final class Cause {
    public static final long INTERRUPT = (1L << 63);

    public static final long SSOFT = INTERRUPT | 1;
    public static final long VSSOFT = INTERRUPT | 2;
    public static final long MSOFT = INTERRUPT | 3;

    public static final long STIMER = INTERRUPT | 5;
    public static final long VSTIMER = INTERRUPT | 6;
    public static final long MTIMER = INTERRUPT | 7;

    public static final long SEXTERNAL = INTERRUPT | 9;
    public static final long VSEXTERNAL = INTERRUPT | 10;
    public static final long MEXTERNAL = INTERRUPT | 11;

    public static final long INST_ADDR_MISALIGNED = 0;
    public static final long INST_ACCESS_FAULT = 1;
    public static final long ILLEGAL_INST = 2;
    public static final long BREAKPOINT = 3;
    public static final long LOAD_ADDR_MISALIGNED = 4;
    public static final long LOAD_ACCESS_FAULT = 5;
    public static final long STORE_ADDR_MISALIGNED = 6;
    public static final long STORE_ACCESS_FAULT = 7;
    public static final long ECALL_FROM_U_MODE = 8;
    public static final long ECALL_FROM_S_MODE = 9;
    public static final long ECALL_FROM_VS_MODE = 10;
    public static final long ECALL_FROM_M_MODE = 11;
    public static final long INSTRUCTION_PAGE_FAULT = 12;
    public static final long LOAD_PAGE_FAULT = 13;
    public static final long STORE_PAGE_FAULT = 15;
  }

  private InterruptException(long pc, long cause, long stval) {
    this.pc = pc;
    this.cause = cause;
    this.stval = stval;
  }

  @TruffleBoundary
  public static InterruptException create(long pc, long cause) {
    return new InterruptException(pc, cause, 0L);
  }

  @TruffleBoundary
  public static InterruptException create(long pc, long cause, long stval) {
    return new InterruptException(pc, cause, stval);
  }

  @TruffleBoundary
  public static InterruptException createIllegalInstruction(long pc) {
    return new InterruptException(pc, Cause.ILLEGAL_INST, pc);
  }
}
