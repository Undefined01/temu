package website.lihan.temu.cpu.csr;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import website.lihan.temu.Utils;

@ExportLibrary(CsrLibrary.class)
public class SStatus {
  private MStatus value;

  public static final long mask =
      MStatus.SIE_MASK | MStatus.SPIE_MASK | MStatus.SPP_MASK | MStatus.SUM_MASK;

  public SStatus(MStatus value) {
    this.value = value;
  }

  @ExportMessage
  public long getValue() {
    return value.getValue();
  }

  @ExportMessage
  public void setValue(long newValue) {
    var diff = value.getValue() ^ newValue;
    if ((diff & ~mask) != 0) {
      Utils.printf("Warning: writing unimplemented bits of sstatus: %016x\n", diff & ~mask);
    }
    value.setValue((value.getValue() & ~mask) | (newValue & mask));
  }

  // Supervisor Previous Privilege Mode
  public int getSPP() {
    return value.getSPP();
  }

  // Supervisor Previous Privilege Mode
  public void setSPP(int spp) {
    value.setSPP(spp);
  }

  // Supervisor Interrupt Enable
  public boolean getSIE() {
    return value.getSIE();
  }

  // Supervisor Interrupt Enable
  public void setSIE(boolean sie) {
    value.setSIE(sie);
  }

  // Supervisor Previous Interrupt Enable
  public boolean getSPIE() {
    return value.getSPIE();
  }

  // Supervisor Previous Interrupt Enable
  public void setSPIE(boolean mpie) {
    value.setSPIE(mpie);
  }
}
