package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.ExecPageCache;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.csr.Satp;
import website.lihan.temu.cpu.csr.SatpDef;
import website.lihan.temu.cpu.instr.MmuSv39.TranslationResult;
import website.lihan.temu.device.Bus;
import website.lihan.temu.device.DeviceLibrary;
import website.lihan.temu.mm.AccessKind;
import website.lihan.temu.mm.Mapping;
import website.lihan.temu.mm.MemoryException;

public final class MemoryAccess {
  public static Mapping queryAddr(
      Rv64State cpu, Bus bus, long vAddr, AccessKind kind, boolean cached) {
    var satp = cpu.getCsrFile().satp;

    var translationResult =
        switch (satp.getMODE()) {
          case SatpDef.MODE_BARE ->
              new TranslationResult(MemoryException.None, 0L, (Long.MAX_VALUE) >> 1);
          case SatpDef.MODE_SV39 -> MmuSv39.getPhysicalAddressSpace(bus, satp, vAddr, kind);
          default -> throw CompilerDirectives.shouldNotReachHere();
        };

    if (translationResult.exception() == MemoryException.None) {
      var pAddr = translationResult.translate(vAddr);
      var device = bus.findDevice(pAddr);
      var deviceLib = DeviceLibrary.create(device, cached);

      var deviceStart = deviceLib.getStartAddress(device);
      var deviceEnd = deviceLib.getEndAddress(device);
      var dAddr = pAddr - deviceStart;

      // Limit the physical address range by the device range
      var pAddrStart = Math.max(deviceStart, translationResult.pAddrStart());
      var pAddrEnd = Math.min(deviceEnd, pAddrStart + translationResult.addrMask() + 1);

      // Limit the page size to maxPageSize
      var maxPageSize = ExecPageCache.MAX_PAGE_SIZE;
      if ((maxPageSize & (maxPageSize - 1)) != 0) {
        throw CompilerDirectives.shouldNotReachHere();
      }
      if (pAddr - pAddrStart >= maxPageSize) {
        pAddrStart = pAddr & ~(maxPageSize - 1);
      }
      pAddrEnd = Math.min(pAddrEnd, pAddrStart + maxPageSize);

      var vAddrStart = vAddr - (pAddr - pAddrStart);
      var vAddrEnd = vAddrStart + (pAddrEnd - pAddrStart);

      return new Mapping(
          MemoryException.None, device, deviceLib, vAddrStart, vAddrEnd, pAddrStart, dAddr - vAddr);
    } else {
      return new Mapping(translationResult.exception(), null, null, 0L, 0L, 0L, 0L);
    }
  }

  @TruffleBoundary
  public static void dumpPageTable(Rv64Context context, Satp satp) {
    var mode = satp.getMODE();

    if (mode == SatpDef.MODE_BARE) {
      return;
    } else if (mode == SatpDef.MODE_SV39) {
      MmuSv39.dumpPageTable(context, satp);
    } else {
      Utils.printf("Unsupported satp mode\n");
    }
  }
}
