package website.lihan.temu.cpu.csr;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(CsrLibrary.class)
public class Satp {
  public static final long MODE_SHIFT = 60;
  public static final long MODE_BITS = 4;
  public static final long MODE_MASK = (1L << MODE_BITS) - 1;
  public static final long ASID_SHIFT = 44;
  public static final long ASID_BITS = 16;
  public static final long ASID_MASK = (1L << ASID_BITS) - 1;
  public static final long PPN_SHIFT = 0;
  public static final long PPN_BITS = 44;
  public static final long PPN_MASK = (1L << PPN_BITS) - 1;

  public static final long MODE_BARE = 0;
  public static final long MODE_SV39 = 8;

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

    var asid = (newValue >> ASID_SHIFT) & ASID_MASK;
    if (asid != 0) {
      newValue = (newValue & ~(ASID_MASK << ASID_SHIFT));
    }

    this.value = newValue;
  }
}
