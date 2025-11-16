package website.lihan.temu.cpu.instr;

import static website.lihan.temu.cpu.RvUtils.BYTES;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.cpu.RvUtils;
import website.lihan.temu.cpu.csr.Satp;
import website.lihan.temu.device.Bus;
import website.lihan.temu.mm.AccessKind;
import website.lihan.temu.mm.MemoryException;
import website.lihan.temu.mm.VaddrToPaddr;

public final class MmuSv39 extends Node {
  private static final int PAGE_BITS = 12;
  private static final long PAGE_MASK = (1L << PAGE_BITS) - 1;
  private static final long PPN_MASK = (1L << 44) - 1;
  private static final int VPN_BITS = 9;
  private static final long VPN_MASK = (1L << VPN_BITS) - 1;
  private static final int PAGE_SIZE = 1 << PAGE_BITS;
  private static final int PTE_SIZE = 8;

  private static final long PTE_V = 1L << 0; // Valid
  private static final long PTE_R = 1L << 1; // Can Read
  private static final long PTE_W = 1L << 2; // Can Write
  private static final long PTE_X = 1L << 3; // Can Execute
  private static final long PTE_U = 1L << 4; // Accessible from User Mode
  private static final long PTE_G = 1L << 5; // Global Mapping
  private static final long PTE_A = 1L << 6; // Accessed
  private static final long PTE_D = 1L << 7; // Dirty
  private static final long PTE_LEAF = PTE_R | PTE_W | PTE_X;

  @ExplodeLoop
  public static VaddrToPaddr translate(Bus bus, Satp satp, long vaddr, AccessKind kind) {
    if (((vaddr << (64 - 39)) >> (64 - 39)) != vaddr) {
      return VaddrToPaddr.invalid(MemoryException.AccessFault);
    }

    var ppn = satp.getPPN() << PAGE_BITS;

    var data = new byte[8];
    int level = 0;
    long pte = 0;

    for (level = 2; level >= 0; level--) {
      var vpn = getVpn(vaddr, level);
      bus.executeRead(ppn | vpn, data, PTE_SIZE);
      pte = RvUtils.BYTES.getLong(data, 0);
      ppn = ((pte >>> 10) & PPN_MASK) << PAGE_BITS;
      if ((pte & PTE_V) == 0) {
        return VaddrToPaddr.invalid(MemoryException.PageFault);
      }
      if ((pte & PTE_LEAF) != 0) {
        break;
      }
    }

    if (level < 0) {
      return VaddrToPaddr.invalid(MemoryException.PageFault);
    }

    var mask = (1L << (12 + level * VPN_BITS)) - 1;
    if ((ppn & mask) != 0) {
      return VaddrToPaddr.invalid(MemoryException.PageFault);
    }
    if (kind == AccessKind.Load && (pte & PTE_R) == 0) {
      return VaddrToPaddr.invalid(MemoryException.PageFault);
    }
    if (kind == AccessKind.Store && (pte & PTE_W) == 0) {
      return VaddrToPaddr.invalid(MemoryException.PageFault);
    }
    if (kind == AccessKind.Execute && (pte & PTE_X) == 0) {
      return VaddrToPaddr.invalid(MemoryException.PageFault);
    }
    var baseAddr = (ppn & ~mask) | (vaddr & mask & ~PAGE_MASK);
    return VaddrToPaddr.create(baseAddr, mask);
  }

  private static long getVpn(long vAddr, long level) {
    return ((vAddr >>> (level * VPN_BITS + PAGE_BITS)) & VPN_MASK) * PTE_SIZE;
  }

  @TruffleBoundary
  public static void dumpPageTable(Rv64Context context, Satp satp) {
    var ppn = satp.getPPN() << PAGE_BITS;
    System.err.printf("Sv39 Page Table, satp=%016x, ppn=%08x:\n", satp, ppn);
    MmuSv39.dumpPageTable(context, ppn, 0, 2, "");
  }

  private static void dumpPageTable(
      Rv64Context context, long ppn, long vAddr, int level, String indent) {
    var buffer = new byte[PAGE_SIZE];
    context.bus.executeRead(ppn, buffer, PAGE_SIZE);
    for (int i = 0; i < PAGE_SIZE / PTE_SIZE; i++) {
      var pte = BYTES.getLong(buffer, i * PTE_SIZE);
      if ((pte & PTE_V) != 0) {
        var nextPpn = ((pte >>> 10) & PPN_MASK) << PAGE_BITS;
        var nextVAddr = vAddr | ((long) i << (level * VPN_BITS + PAGE_BITS));
        nextVAddr = nextVAddr << (64 - 39) >> (64 - 39);
        var nextVAddrEnd = nextVAddr + (1L << (level * VPN_BITS + PAGE_BITS));
        if (level > 0 && (pte & PTE_LEAF) == 0) {
          System.err.printf(
              "%sPTE%d[%03x]: %016x, vrange=[%08x, %08x), nextPpn=%08x\n",
              indent, level, i, pte, nextVAddr, nextVAddrEnd, nextPpn);
          dumpPageTable(context, nextPpn, nextVAddr, level - 1, indent + "  ");
        } else {
          System.err.printf(
              "%sPTE%d[%03x]: %016x, vrange=[%08x, %08x) -> %08x, perm=",
              indent, level, i, pte, nextVAddr, nextVAddrEnd, nextPpn);
          if ((pte & PTE_R) != 0) {
            System.err.printf("R");
          }
          if ((pte & PTE_W) != 0) {
            System.err.printf("W");
          }
          if ((pte & PTE_X) != 0) {
            System.err.printf("X");
          }
          if ((pte & PTE_U) != 0) {
            System.err.printf("U");
          }
          if ((pte & PTE_G) != 0) {
            System.err.printf("G");
          }
          if ((pte & PTE_A) != 0) {
            System.err.printf("A");
          }
          if ((pte & PTE_D) != 0) {
            System.err.printf("D");
          }
          System.err.printf("\n");
        }
      }
    }
  }
}
