package website.lihan.temu.mm;

import com.oracle.truffle.api.CompilerDirectives;
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.ExecPageCache;
import website.lihan.temu.cpu.InterruptException;
import website.lihan.temu.device.Bus;
import website.lihan.temu.device.DeviceLibrary;

public record Mapping(
    MemoryException exception,
    Object device,
    DeviceLibrary deviceLib,
    long vAddrStart,
    long vAddrEnd,
    long pAddrStart,
    long v2dOffset) {

  public static Mapping from(Bus bus, long vAddr, VaddrToPaddr v2p, boolean cached) {
    if (v2p.exception() == null) {
      var pAddr = v2p.translate(vAddr);
      var device = bus.findDevice(pAddr);
      var deviceLib = DeviceLibrary.create(device, cached);

      var deviceStart = deviceLib.getStartAddress(device);
      var deviceEnd = deviceLib.getEndAddress(device);
      var dAddr = pAddr - deviceStart;

      // Limit the physical address range by the device range
      var pAddrStart = Utils.unsignedMax(deviceStart, v2p.pAddrStart());
      var pAddrEnd = Utils.unsignedMin(deviceEnd, pAddrStart + v2p.addrMask() + 1);

      // Limit the page size to maxPageSize
      var maxPageSize = ExecPageCache.MAX_PAGE_SIZE;
      if ((maxPageSize & (maxPageSize - 1)) != 0) {
        throw CompilerDirectives.shouldNotReachHere();
      }
      if (pAddr - pAddrStart >= maxPageSize) {
        pAddrStart = pAddr & ~(maxPageSize - 1);
      }
      pAddrEnd = Utils.unsignedMin(pAddrEnd, pAddrStart + maxPageSize);

      var vAddrStart = vAddr - (pAddr - pAddrStart);
      var vAddrEnd = vAddrStart + (pAddrEnd - pAddrStart);

      return new Mapping(null, device, deviceLib, vAddrStart, vAddrEnd, pAddrStart, dAddr - vAddr);
    } else {
      return new Mapping(v2p.exception(), null, null, 0L, 0L, 0L, 0L);
    }
  }

  public boolean inRange(long vAddr) {
    return vAddrStart <= vAddr && vAddr < vAddrEnd;
  }

  public long toPAddr(long vAddr) {
    if (exception != null) {
      throw CompilerDirectives.shouldNotReachHere();
    }
    return vAddr - vAddrStart + pAddrStart;
  }

  public long load(long pc, long vAddr, int length, boolean signedExtend) {
    if (exception != null) {
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
    if (exception != null) {
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
