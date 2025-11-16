package website.lihan.temu.cpu.csr;

public final class Misa {
  public static final long EXT_A = 1L << ('A' - 'A'); // / Atomic extension
  public static final long EXT_C = 1L << ('C' - 'A'); // / Compressed extension
  public static final long EXT_D = 1L << ('D' - 'A'); // / Double-precision floating-point extension
  public static final long EXT_F = 1L << ('F' - 'A'); // / Single-precision floating-point extension
  public static final long EXT_I = 1L << ('I' - 'A'); // / Base integer ISA
  public static final long EXT_M =
      1L << ('M' - 'A'); // / Integer multiplication and division extension
  public static final long EXT_S = 1L << ('S' - 'A'); // / Supervisor mode extension
  public static final long EXT_U = 1L << ('U' - 'A'); // / User mode extension
  public static final long EXT_V = 1L << ('V' - 'A'); // / Vector extension
  public static final long EXT_X = 1L << ('X' - 'A'); // / Non-standard extensions

  public static final long XLEN_32 = 1L << 30;
  public static final long XLEN_64 = 2L << 62;

  public static final long VALUE = XLEN_64 | EXT_I | EXT_M | EXT_A | EXT_S | EXT_U;

  private Misa() {}
}
