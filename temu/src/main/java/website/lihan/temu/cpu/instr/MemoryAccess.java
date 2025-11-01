package website.lihan.temu.cpu.instr;

import website.lihan.temu.cpu.InterruptException;
import website.lihan.temu.cpu.InterruptException.Cause;

import com.oracle.truffle.api.CompilerDirectives;

import website.lihan.temu.Utils;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.device.Bus;
import website.lihan.temu.device.DeviceLibrary;

public class MemoryAccess {
  public static Mapping queryAddr(Rv64State cpu, Bus bus, long vAddr, AccessKind kind, boolean cached) {
    var satp = cpu.getCsrFile().satp.getValue();
    if (satp == 0) {
      var device = bus.findDevice(vAddr);
      DeviceLibrary deviceLib;
      if (cached) {
        deviceLib = DeviceLibrary.getFactory().create(device);
      } else {
        deviceLib = DeviceLibrary.getFactory().getUncached();
      }
      var startAddr = deviceLib.getStartAddress(device);
      var endAddr = deviceLib.getEndAddress(device);
      return new Mapping(MemoryException.None, device, deviceLib, startAddr, endAddr, startAddr, -startAddr);
    } else {
      var result = MmuRv39.getPhysicalAddressSpace(bus, satp, vAddr, kind);
      if (result.exception() == MemoryException.None) {
        var pAddr = result.translate(vAddr);
        var device = bus.findDevice(pAddr);
        DeviceLibrary deviceLib;
        if (cached) {
          deviceLib = DeviceLibrary.getFactory().create(device);
        } else {
          deviceLib = DeviceLibrary.getFactory().getUncached();
        }
        var deviceStart = deviceLib.getStartAddress(device);
        var deviceEnd = deviceLib.getEndAddress(device);
        var dAddr = pAddr - deviceStart;
        // Limit the physical address range by the device range
        var pAddrStart = Math.max(deviceStart, result.pAddrStart());
        var pAddrEnd = Math.min(deviceEnd, pAddrStart + result.addrMask() + 1);
        var vAddrStart = vAddr - (pAddr - pAddrStart);
        var vAddrEnd = vAddrStart + (pAddrEnd - pAddrStart);
        return new Mapping(MemoryException.None, device, deviceLib, vAddrStart, vAddrEnd, pAddrStart, dAddr - vAddr);
      } else {
        return new Mapping(result.exception(), null, null, 0L, 0L, 0L, 0L);
      }
    }
  }

  public record Mapping(MemoryException exception, Object device, DeviceLibrary deviceLib, long vAddrStart, long vAddrEnd, long pAddrStart, long v2dOffset) {
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

  public enum AccessKind {
    Load,
    Store,
    Execute,
  }

  public enum MemoryException {
    None,
    AccessFault,
    PageFault,
    ;

    public long toLoadException() {
      return switch (this) {
        case AccessFault -> Cause.LOAD_ACCESS_FAULT;
        case PageFault -> Cause.LOAD_PAGE_FAULT;
        default -> throw CompilerDirectives.shouldNotReachHere();
      };
    }

    public long toStoreException() {
      return switch (this) {
        case AccessFault -> Cause.STORE_ACCESS_FAULT;
        case PageFault -> Cause.STORE_PAGE_FAULT;
        default -> throw CompilerDirectives.shouldNotReachHere();
      };
    }
  }
}
