package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ControlFlowException;

public class IllegalInstructionException extends ControlFlowException {
  private long pc = 0;
  private int instr = 0;
  private String message = null;

  public IllegalInstructionException() {
    super();
  }

  @TruffleBoundary
  public static IllegalInstructionException create() {
    return new IllegalInstructionException();
  }

  @TruffleBoundary
  public static IllegalInstructionException create(String format, Object... args) {
    var exception = new IllegalInstructionException();
    exception.message = String.format(format, args);
    return exception;
  }

  @TruffleBoundary
  public static IllegalInstructionException create(long pc, int instr) {
    var exception = new IllegalInstructionException();
    exception.pc = pc;
    exception.instr = instr;
    return exception;
  }

  @Override
  public String getMessage() {
    if (message != null) {
      return message;
    }
    return "Illegal instruction at " + Long.toHexString(pc) + ": " + Integer.toHexString(instr);
  }
}
