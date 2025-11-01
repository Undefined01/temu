package website.lihan.temu.cpu.csr;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(CsrLibrary.class)
public class Satp {
  private static final long MODE_SHIFT = 60;
  private static final long MODE_BITS = 4;
  private static final long MODE_MASK = (1L << MODE_BITS) - 1;

  private static final long MODE_BARE = 0;
  private static final long MODE_SV39 = 8;

  private long value;

  public Satp() {
    this(0);
  }

  public Satp(long initialValue) {
    this.value = initialValue;
  }

  @ExportMessage
  public long getValue() {
    return value;
  }

  @ExportMessage
  public void setValue(long newValue) {
    var mode = (newValue >> MODE_SHIFT) & MODE_MASK;
    if (mode != MODE_BARE && mode != MODE_SV39) {
      newValue = 0;
    }
    this.value = newValue;
  }
}
