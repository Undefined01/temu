package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;

import website.lihan.temu.Rv64Context;
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.RvUtils;
import website.lihan.temu.cpu.instr.MemoryAccess.AccessKind;
import website.lihan.temu.cpu.instr.MemoryAccess.MemoryException;
import website.lihan.temu.device.Bus;

public final class MmuRv39 extends Node {
    private static final int PAGE_BITS = 12;
    private static final long PAGE_MASK = (1L << PAGE_BITS) - 1;
    private static final long PPN_MASK = (1L << 44) - 1;
    private static final int VPN_BITS = 9;
    private static final long VPN_MASK = (1L << VPN_BITS) - 1;

    private static final long PTE_V = 1L << 0;  // Valid
    private static final long PTE_R = 1L << 1;  // Can Read
    private static final long PTE_W = 1L << 2;  // Can Write
    private static final long PTE_X = 1L << 3;  // Can Execute
    private static final long PTE_U = 1L << 4;  // Accessible from User Mode
    private static final long PTE_G = 1L << 5;  // Global Mapping
    private static final long PTE_A = 1L << 6;  // Accessed
    private static final long PTE_D = 1L << 7;  // Dirty

    @ExplodeLoop
    public static TranslationResult getPhysicalAddressSpace(Bus bus, long satp, long vaddr, AccessKind kind) {
        if (((vaddr << (64 - 39)) >> (64 - 39)) != vaddr) {
            return TranslationResult.invalid(MemoryException.AccessFault);
        }

        var ppn = (satp & PPN_MASK) << 12;

        var data = new byte[8];
        int level = 0;
        long pte = 0;

        for (level = 2; level >= 0; level--) {
            var vpn = ((vaddr >>> (12 + level * VPN_BITS)) & VPN_MASK) << 3;
            bus.executeRead(ppn + vpn, data, 8);
            pte = RvUtils.BYTES.getLong(data, 0);
            ppn = ((pte >>> 10) & PPN_MASK) << 12;
            if ((pte & PTE_V) == 0) {
                return TranslationResult.invalid(MemoryException.PageFault);
            }
            if ((pte & (PTE_R | PTE_W | PTE_X)) != 0) {
                break;
            }
        }

        if (level < 0) {
            return TranslationResult.invalid(MemoryException.PageFault);
        }

        var mask = (1L << (12 + level * VPN_BITS)) - 1;
        if ((ppn & mask) != 0) {
            return TranslationResult.invalid(MemoryException.PageFault);
        }
        if (kind == AccessKind.Load && (pte & PTE_R) == 0) {
            return TranslationResult.invalid(MemoryException.PageFault);
        }
        if (kind == AccessKind.Store && (pte & PTE_W) == 0) {
            return TranslationResult.invalid(MemoryException.PageFault);
        }
        if (kind == AccessKind.Execute && (pte & PTE_X) == 0) {
            return TranslationResult.invalid(MemoryException.PageFault);
        }
        var baseAddr = (ppn & ~mask) | (vaddr & mask & ~PAGE_MASK);
        return new TranslationResult(MemoryException.None, baseAddr, mask, pte);
    }

    public record TranslationResult(MemoryException exception, long pAddrStart, long addrMask, long pte) {
        public static TranslationResult invalid(MemoryException exception) {
            return new TranslationResult(exception, 0L, 0L, 0L);
        }

        public long translate(long vaddr) {
            return pAddrStart | (vaddr & addrMask);
        }
    }
}
