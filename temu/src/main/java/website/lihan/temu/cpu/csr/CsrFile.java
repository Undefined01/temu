package website.lihan.temu.cpu.csr;

import static website.lihan.temu.cpu.csr.CsrId.*;

public class CsrFile {
  public final MStatus status = new MStatus();

  public final Dummy mhartid = new Dummy();

  public final SStatus sstatus = new SStatus(status);
  public final Dummy sie = new Dummy();
  public final Dummy stvec = new Dummy();
  public final Dummy sscratch = new Dummy();
  public final Dummy sepc = new Dummy();
  public final Dummy scause = new Dummy();
  public final Dummy stval = new Dummy();
  public final Dummy sip = new Dummy();
  public final Dummy satp = new Dummy();

  public final MStatus mstatus = status;
  public final Dummy misa = new Dummy();
  public final Dummy mie = new Dummy();
  public final Dummy mtvec = new Dummy();
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
      case SSCRATCH -> sscratch;
      case SEPC -> sepc;
      case SCAUSE -> scause;
      case STVAL -> stval;
      case SIP -> sip;
      case SATP -> satp;

      case MSTATUS -> mstatus;
      case MISA -> misa;
      case MIE -> mie;
      case MTVEC -> mtvec;
      case MSCRATCH -> mscratch;
      case MEPC -> mepc;
      case MCAUSE -> mcause;
      case MTVAL -> mtval;

      case RDTIME -> rdtime;

      case MHARTID -> mhartid;

      default -> null;
    };
  }
}
