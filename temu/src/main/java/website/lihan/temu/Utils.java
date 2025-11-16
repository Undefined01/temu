package website.lihan.temu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class Utils {
  @TruffleBoundary
  public static void printf(String format, Object... args) {
    System.err.printf(format, args);
  }

  @TruffleBoundary
  public static void printf(String format, long arg1) {
    System.err.printf(format, arg1);
  }

  @TruffleBoundary
  public static void printf(String format, long arg1, long arg2) {
    System.err.printf(format, arg1, arg2);
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
