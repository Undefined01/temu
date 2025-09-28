package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class IllegalInstructionException extends RuntimeException {
  private IllegalInstructionException() {
    super();
  }

  private IllegalInstructionException(String message) {
    super(message);
  }

  @TruffleBoundary
  public static IllegalInstructionException create() {
    return new IllegalInstructionException();
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
}
