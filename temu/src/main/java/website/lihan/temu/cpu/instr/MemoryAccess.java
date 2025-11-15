package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.csr.Satp;
import website.lihan.temu.device.Bus;
import website.lihan.temu.device.DeviceLibrary;
import website.lihan.temu.mm.AccessKind;
import website.lihan.temu.mm.Mapping;
import website.lihan.temu.mm.MemoryException;

public class MemoryAccess {
  public static Mapping queryAddr(
      Rv64State cpu, Bus bus, long vAddr, AccessKind kind, boolean cached) {
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
      return new Mapping(
          MemoryException.None, device, deviceLib, startAddr, endAddr, startAddr, -startAddr);
    } else {
      var result = MmuSv39.getPhysicalAddressSpace(bus, satp, vAddr, kind);
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
        return new Mapping(
            MemoryException.None,
            device,
            deviceLib,
            vAddrStart,
            vAddrEnd,
            pAddrStart,
            dAddr - vAddr);
      } else {
        return new Mapping(result.exception(), null, null, 0L, 0L, 0L, 0L);
      }
    }
  }

  @TruffleBoundary
  public static void dumpPageTable(Rv64Context context, long satp) {
    var mode = (satp >> Satp.MODE_SHIFT) & Satp.MODE_MASK;

    if (mode == Satp.MODE_BARE) {
      return;
    } else if (mode == Satp.MODE_SV39) {
      MmuSv39.dumpPageTable(context, satp);
    } else {
      Utils.printf("Unsupported satp mode\n");
    }
  }
}
