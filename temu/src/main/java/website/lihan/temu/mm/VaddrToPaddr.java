package website.lihan.temu.mm;

public record VaddrToPaddr(MemoryException exception, long pAddrStart, long addrMask) {
  public static VaddrToPaddr invalid(MemoryException exception) {
    return new VaddrToPaddr(exception, 0L, 0L);
  }

  public static VaddrToPaddr create(long pAddrStart, long addrMask) {
    return new VaddrToPaddr(null, pAddrStart, addrMask);
  }

  public long translate(long vaddr) {
    return pAddrStart | (vaddr & addrMask);
  }
}
