package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ControlFlowException;

public class IllegalInstructionException extends ControlFlowException {
  private final String message;

  private IllegalInstructionException(String message) {
    super();
    this.message = message;
  }

  @TruffleBoundary
  public static IllegalInstructionException create(String format, Object... args) {
    return new IllegalInstructionException(String.format(format, args));
  }

  @TruffleBoundary
  public static IllegalInstructionException create(long pc, int instr) {
    return new IllegalInstructionException(
        String.format("Illegal instruction 0x%08x at pc=0x%08x", instr, pc));
  }

  @Override
  @TruffleBoundary
  public String getMessage() {
    return message;
  }
}
