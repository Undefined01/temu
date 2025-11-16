package website.lihan.temu.cpu.csr;

import website.lihan.temu.annotations.BitField;
import website.lihan.temu.annotations.RiscvCsr;
import website.lihan.temu.annotations.Warl;

@RiscvCsr
public abstract class TVecDef {
  public static final int MODE_DIRECT = 0;
  public static final int MODE_VECTORED = 1;

  @BitField(offset = 0, length = 2)
  @Warl(values = {MODE_DIRECT, MODE_VECTORED})
  private int mode;

  @BitField(offset = 2, length = 62)
  private int base;
}
