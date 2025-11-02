package website.lihan.temu.cpu.csr;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import website.lihan.temu.Utils;

@ExportLibrary(CsrLibrary.class)
public class Dummy {
  private long value;
  private final String name;
  private final long validMask;
  private final long unimplementedMask;

  public Dummy() {
    this(0);
  }

  public Dummy(long initialValue) {
    this.name = "Dummy CSR";
    this.value = initialValue;
    this.validMask = -1L;
    this.unimplementedMask = 0L;
  }

  public Dummy(String name, long initialValue, long validMask, long unimplementedMask) {
    this.name = name;
    this.value = initialValue;
    this.validMask = validMask;
    this.unimplementedMask = unimplementedMask;
  }

  @ExportMessage
  public long getValue() {
    return value;
  }

  @ExportMessage
  public void setValue(long newValue) {
    if (validMask != -1L) {
      var diff = value ^ newValue;
      if ((diff & unimplementedMask) != 0) {
        Utils.printf(
            "Warning: Attempt to write to unimplemented bits of CSR %s. newValue=0x%016X, unimplementedMask=0x%016X\n",
            name, newValue, unimplementedMask);
      }
      if ((diff & ~validMask) != 0) {
        newValue = (newValue & validMask) | (value & ~validMask);
      }
    }
    this.value = newValue;
  }
}
