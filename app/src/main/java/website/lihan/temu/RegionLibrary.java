package website.lihan.temu;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.Library;

@GenerateLibrary
public abstract class RegionLibrary extends Library {
    public abstract int read(Object receiver, int address, byte[] data);
    public abstract int write(Object receiver, int address, byte[] data);
}
