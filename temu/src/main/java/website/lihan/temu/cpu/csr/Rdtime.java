package website.lihan.temu.cpu.csr;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import website.lihan.temu.Utils;
import website.lihan.temu.device.RTC;

@ExportLibrary(CsrLibrary.class)
public class Rdtime {
    @ExportMessage
    public long getValue() {
        return RTC.getTime();
    }

    @ExportMessage
    public void setValue(long newValue) {
    }
}
