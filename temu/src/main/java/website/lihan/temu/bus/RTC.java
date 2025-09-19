package website.lihan.temu.bus;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.memory.ByteArraySupport;
import website.lihan.temu.Utils;

public class RTC implements Region {
  public final long baseAddress;

  private static final ByteArraySupport BYTES = ByteArraySupport.littleEndian();

  public RTC() {
    this(0xa0000048L);
  }

  public RTC(long baseAddress) {
    this.baseAddress = baseAddress;
  }

  @Override
  public long getStartAddress() {
    return baseAddress;
  }

  @Override
  public long getEndAddress() {
    return baseAddress + 8;
  }

  @Override
  @TruffleBoundary
  public int read(long address, byte[] data, int length) {
    if (length > 8)
      return -1;

    long microseconds = System.currentTimeMillis() * 1000;
    byte[] temp = new byte[8];
    BYTES.putLong(temp, 0, microseconds);
    System.arraycopy(temp, 0, data, 0, length);
    return length;
  }

  @Override
  public int write(long address, byte[] data, int length) {
    return -1;
  }
}
