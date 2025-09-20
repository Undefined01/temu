package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ControlFlowException;

public class JumpException extends ControlFlowException {
  private long pc = 0;

  private JumpException() {
    super();
  }

  @TruffleBoundary
  public static JumpException create(long pc) {
    var exception = new JumpException();
    exception.pc = pc;
    return exception;
  }

  public long getTargetPc() {
    return pc;
  }

  @Override
  public String getMessage() {
    return String.format("Jump to %08x", pc);
  }
}
