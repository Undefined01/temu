package website.lihan.temu.device;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(DeviceLibrary.class)
public final class SerialPort {
  private final long baseAddress;

  public SerialPort() {
    this(0xa00003f8L);
  }

  public SerialPort(long baseAddress) {
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
  public int read(long address, byte[] data, int length) {
    return -1;
  }

  @ExportMessage
  @TruffleBoundary
  public int write(long address, byte[] data, int length) {
    if (address == 0 && length == 1) {
      System.out.print((char) data[0]);
      return 0;
    }
    return -1;
  }

  @ExportMessage
  @TruffleBoundary
  public void write1(long address, byte data) {
    System.out.print((char) data);
  }
}
