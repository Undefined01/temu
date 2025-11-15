package website.lihan.temu.cpu.csr;

import website.lihan.temu.annotations.BitField;
import website.lihan.temu.annotations.RiscvCsr;
import website.lihan.temu.annotations.Warl;

@RiscvCsr(address = 0x300)
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
  private int mpp = 3;

  @BitField(offset = 18, length = 1)
  private boolean sum;
}
