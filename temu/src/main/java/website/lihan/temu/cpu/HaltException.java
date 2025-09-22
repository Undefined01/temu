package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ControlFlowException;

public class HaltException extends ControlFlowException {
  private final long pc;
  private final long exitCode;

  private HaltException(long pc, long exitCode) {
    this.pc = pc;
    this.exitCode = exitCode;
  }

  @TruffleBoundary
  public static HaltException create(long pc, long exitCode) {
    return new HaltException(pc, exitCode);
  }

  @Override
  @TruffleBoundary
  public String getMessage() {
    return String.format("Halt at %08x with code %d", pc, exitCode);
  }
}
