package website.lihan.temu.device;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import website.lihan.temu.Utils;

@ExportLibrary(DeviceLibrary.class)
public final class InvalidDevice {
  @ExportMessage
  public long getStartAddress() {
    return 0;
  }

  @ExportMessage
  public long getEndAddress() {
    return 0;
  }

  @ExportMessage
  public int read(long address, byte[] data, int length) {
    Utils.printf("[Bus] read from invalid address: 0x%08x\n", address);
    return -1;
  }

  @ExportMessage
  public int write(long address, byte[] data, int length) {
    Utils.printf("[Bus] write to invalid address: 0x%08x\n", address);
    return -1;
  }
}
