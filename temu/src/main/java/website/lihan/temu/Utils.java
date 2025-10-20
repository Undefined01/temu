package website.lihan.temu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class Utils {
  @TruffleBoundary
  public static void printf(String format, Object... args) {
    System.err.printf(format, args);
  }

  @TruffleBoundary
  public static void printStackTrace(Throwable t) {
    t.printStackTrace();
  }

  public static long signedUnsignedMultiplyHigh(long s, long u) {
    long high = Math.multiplyHigh(s, u);
    if (u < 0) {
      high += s;
    }
    return high;
  }
}
