package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class InternalException extends RuntimeException {
  private InternalException(String message) {
    super(message);
  }

  @TruffleBoundary
  public static InternalException create(String message) {
    return new InternalException(message);
  }

  @TruffleBoundary
  public static InternalException fromPc(long pc, String message) {
    return new InternalException(String.format("At pc=0x%016x: %s", pc, message));
  }
}
