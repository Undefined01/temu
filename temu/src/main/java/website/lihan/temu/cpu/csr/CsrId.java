package website.lihan.temu.cpu.csr;

public final class CsrId {
  public static final int SSTATUS = 0x100;
  public static final int SIE = 0x104;
  public static final int STVEC = 0x105;
  public static final int SCOUNTEREN = 0x106;
  public static final int SSCRATCH = 0x140;
  public static final int SEPC = 0x141;
  public static final int SCAUSE = 0x142;
  public static final int STVAL = 0x143;
  public static final int SIP = 0x144;
  public static final int SATP = 0x180;

  public static final int MSTATUS = 0x300;
  public static final int MISA = 0x301;
  public static final int MEDELEG = 0x302;
  public static final int MIDELEG = 0x303;
  public static final int MIE = 0x304;
  public static final int MTVEC = 0x305;
  public static final int MSCRATCH = 0x340;
  public static final int MEPC = 0x341;
  public static final int MCAUSE = 0x342;
  public static final int MTVAL = 0x343;

  public static final int RDCYCLE = 0xC00;
  public static final int RDTIME = 0xC01;

  public static final int MVENDORID = 0xF11;
  public static final int MARCHID = 0xF12;
  public static final int MIMPID = 0xF13;
  public static final int MHARTID = 0xF14;
}
