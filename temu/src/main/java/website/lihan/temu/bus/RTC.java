package website.lihan.temu.bus;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.memory.ByteArraySupport;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import website.lihan.temu.Utils;

@ExportLibrary(RegionLibrary.class)
public final class RTC {
  public final long baseAddress;

  private static final ByteArraySupport BYTES = ByteArraySupport.littleEndian();

  public RTC() {
    this(0xa0000048L);
  }

  public RTC(long baseAddress) {
    this.baseAddress = baseAddress;
  }

  @ExportMessage
  public long getStartAddress() {
    return baseAddress;
  }

  @ExportMessage
  public long getEndAddress() {
    return baseAddress + 8;
  }

  @ExportMessage
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

  @ExportMessage
  public int write(long address, byte[] data, int length) {
    return -1;
  }
}
