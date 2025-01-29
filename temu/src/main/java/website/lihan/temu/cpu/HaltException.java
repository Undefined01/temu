package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ControlFlowException;

public class HaltException extends ControlFlowException {
  private long pc = 0;

  public HaltException() {
    super();
  }

  @TruffleBoundary
  public static HaltException create(long pc) {
    var exception = new HaltException();
    exception.pc = pc;
    return exception;
  }

  @Override
  @TruffleBoundary
  public String getMessage() {
    return String.format("Halt at %08x", pc);
  }
}
