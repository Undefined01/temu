package website.lihan.temu.cpu.csr;

import static website.lihan.temu.cpu.csr.CsrId.*;

public final class CsrFile {
  public final MStatus status = new MStatus();

  public final Dummy notImplemented = new Dummy("Not Implemented CSR", 0, 0L, -1L);
  public final Dummy mhartid = new Dummy();

  public final SStatus sstatus = new SStatus(status);
  public final Dummy sie = new Dummy("sie", 0, 0x222L, -1L);
  public final TVec stvec = new TVec();
  public final Dummy scounteren = new Dummy();
  public final Dummy sscratch = new Dummy();
  public final Dummy sepc = new Dummy();
  public final Dummy scause = new Dummy();
  public final Dummy stval = new Dummy();
  public final Dummy sip = new Dummy("sip", 0, 0x222L, -1L);
  public final Satp satp = new Satp();

  public final MStatus mstatus = status;
  public final Dummy medeleg = new Dummy();
  public final Dummy mideleg = new Dummy();
  public final Dummy mie = new Dummy();
  public final TVec mtvec = new TVec();
  public final Dummy mscratch = new Dummy();
  public final Dummy mepc = new Dummy();
  public final Dummy mcause = new Dummy();
  public final Dummy mtval = new Dummy();

  public final Rdtime rdtime = new Rdtime();

  public Object getCsrById(int csrId) {
    return switch (csrId) {
      case SSTATUS -> sstatus;
      case SIE -> sie;
      case STVEC -> stvec;
      case SCOUNTEREN -> scounteren;
      case SSCRATCH -> sscratch;
      case SEPC -> sepc;
      case SCAUSE -> scause;
      case STVAL -> stval;
      case SIP -> sip;
      case SATP -> satp;

      case MSTATUS -> mstatus;
      case MEDELEG -> medeleg;
      case MIDELEG -> mideleg;
      case MISA -> notImplemented;
      case MIE -> mie;
      case MTVEC -> mtvec;
      case MSCRATCH -> mscratch;
      case MEPC -> mepc;
      case MCAUSE -> mcause;
      case MTVAL -> mtval;

      case RDTIME -> rdtime;

      case MVENDORID -> notImplemented;
      case MARCHID -> notImplemented;
      case MIMPID -> notImplemented;
      case MHARTID -> mhartid;

      default -> null;
    };
  }
}
