package website.lihan.temu.cpu.csr;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(CsrLibrary.class)
public class Dummy {
    private long value;

    public Dummy() {
        this(0);
    }

    public Dummy(long initialValue) {
        this.value = initialValue;
    }

    @ExportMessage
    public long getValue() {
        return value;
    }

    @ExportMessage
    public void setValue(long newValue) {
        this.value = newValue;
    }
}
