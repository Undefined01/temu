package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ControlFlowException;

public class HaltException extends ControlFlowException {
  private long pc = 0;
  private long exitCode = 0;

  private HaltException() {
    super();
  }

  @TruffleBoundary
  public static HaltException create(long pc, long exitCode) {
    var exception = new HaltException();
    exception.pc = pc;
    exception.exitCode = exitCode;
    return exception;
  }

  @Override
  @TruffleBoundary
  public String getMessage() {
    return String.format("Halt at %08x with code %d", pc, exitCode);
  }
}
