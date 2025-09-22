package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ControlFlowException;

public class InterruptException extends ControlFlowException {
  final public long pc;
  final public long cause;

  private InterruptException(long pc, long cause) {
    this.pc = pc;
    this.cause = cause;
  }

  @TruffleBoundary
  public static InterruptException create(long pc, long cause) {
    return new InterruptException(pc, cause);
  }
}
