package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ControlFlowException;

public class HaltException extends ControlFlowException {
  private final long pc;
  private final long exitCode;
  private final String message;

  private HaltException(long pc, long exitCode) {
    this.pc = pc;
    this.exitCode = exitCode;
    this.message = null;
  }

  private HaltException(long pc, long exitCode, String message) {
    this.pc = pc;
    this.exitCode = exitCode;
    this.message = message;
  }

  @TruffleBoundary
  public static HaltException create(long pc, long exitCode) {
    return new HaltException(pc, exitCode);
  }

  @TruffleBoundary
  public static HaltException create(long pc, long exitCode, String format, Object... args) {
    return new HaltException(pc, exitCode, String.format(format, args));
  }

  @Override
  @TruffleBoundary
  public String getMessage() {
    var res = String.format("Halt at %08x with code %d", pc, exitCode);
    if (message != null) {
      res += ": " + message;
    }
    return res;
  }
}
