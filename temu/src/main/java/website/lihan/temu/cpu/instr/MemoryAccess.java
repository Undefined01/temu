package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.csr.Satp;
import website.lihan.temu.cpu.csr.SatpDef;
import website.lihan.temu.device.Bus;
import website.lihan.temu.mm.AccessKind;
import website.lihan.temu.mm.Mapping;
import website.lihan.temu.mm.VaddrToPaddr;

public final class MemoryAccess {
  public static Mapping queryAddr(
      Rv64State cpu, Bus bus, long vAddr, AccessKind kind, boolean cached) {
    var satp = cpu.getCsrFile().satp;

    var v2p =
        switch (satp.getMODE()) {
          case SatpDef.MODE_BARE -> VaddrToPaddr.create(0L, (Long.MAX_VALUE) >> 1);
          case SatpDef.MODE_SV39 -> MmuSv39.translate(bus, satp, vAddr, kind);
          default -> throw CompilerDirectives.shouldNotReachHere();
        };

    return Mapping.from(bus, vAddr, v2p, cached);
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
