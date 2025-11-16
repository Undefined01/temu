package website.lihan.temu.cpu.csr;

import website.lihan.temu.annotations.BitField;
import website.lihan.temu.annotations.RiscvCsr;
import website.lihan.temu.annotations.Warl;

@RiscvCsr
public abstract class SatpDef {
  public static final int MODE_BARE = 0;
  public static final int MODE_SV39 = 8;

  @BitField(offset = 60, length = 4)
  @Warl(values = {MODE_BARE, MODE_SV39})
  private int mode;

  @BitField(offset = 44, length = 16)
  @Warl(values = {0})
  private int asid;

  @BitField(offset = 0, length = 44)
  private long ppn;
}
