package website.lihan.temu.cpu.csr;

import website.lihan.temu.annotations.BitField;
import website.lihan.temu.annotations.RiscvCsr;
import website.lihan.temu.annotations.Warl;
import website.lihan.temu.annotations.Wpri;

@RiscvCsr
public abstract class MStatusDef {
  @BitField(offset = 0, length = 1)
  private boolean sie;

  @BitField(offset = 3, length = 1, resetValue = 1)
  private boolean mie = true;

  @BitField(offset = 5, length = 1)
  private boolean spie;

  @BitField(offset = 7, length = 1, resetValue = 1)
  private boolean mpie;

  @BitField(offset = 8, length = 1)
  private int spp;

  @BitField(offset = 11, length = 2, resetValue = 3)
  @Warl(values = {0, 1, 3})
  private int mpp;

  @BitField(offset = 18, length = 1)
  private boolean sum;

  @BitField(offset = 32, length = 2, resetValue = 2)
  @Wpri
  private int uxl;

  @BitField(offset = 34, length = 2, resetValue = 2)
  @Wpri
  private int sxl;
}
