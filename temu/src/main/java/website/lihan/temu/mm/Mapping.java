package website.lihan.temu.mm;

import website.lihan.temu.cpu.InterruptException;
import website.lihan.temu.device.DeviceLibrary;

public record Mapping(
    MemoryException exception,
    Object device,
    DeviceLibrary deviceLib,
    long vAddrStart,
    long vAddrEnd,
    long pAddrStart,
    long v2dOffset) {
  public boolean inRange(long vAddr) {
    return vAddrStart <= vAddr && vAddr < vAddrEnd;
  }

  public long toPAddr(long vAddr) {
    return vAddr - vAddrStart + pAddrStart;
  }

  public long load(long pc, long vAddr, int length, boolean signedExtend) {
    if (exception != MemoryException.None) {
      throw InterruptException.create(pc, exception.toLoadException(), vAddr);
    }

    var dAddr = vAddr + v2dOffset;
    if (signedExtend) {
      return switch (length) {
        case 1 -> deviceLib.read1(device, dAddr);
        case 2 -> deviceLib.read2(device, dAddr);
        case 4 -> deviceLib.read4(device, dAddr);
        case 8 -> deviceLib.read8(device, dAddr);
        default -> 0;
      };
    } else {
      return switch (length) {
        case 1 -> (long) deviceLib.read1(device, dAddr) & 0xFFL;
        case 2 -> (long) deviceLib.read2(device, dAddr) & 0xFFFFL;
        case 4 -> (long) deviceLib.read4(device, dAddr) & 0xFFFFFFFFL;
        case 8 -> deviceLib.read8(device, dAddr);
        default -> 0;
      };
    }
  }

  public void store(long pc, long vAddr, int length, long value) {
    if (exception != MemoryException.None) {
      throw InterruptException.create(pc, exception.toStoreException(), vAddr);
    }

    var dAddr = vAddr + v2dOffset;
    switch (length) {
      case 1 -> deviceLib.write1(device, dAddr, (byte) value);
      case 2 -> deviceLib.write2(device, dAddr, (short) value);
      case 4 -> deviceLib.write4(device, dAddr, (int) value);
      case 8 -> deviceLib.write8(device, dAddr, value);
    }
  }
}
