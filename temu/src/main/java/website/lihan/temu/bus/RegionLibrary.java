package website.lihan.temu.bus;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;

@GenerateLibrary
public abstract class RegionLibrary extends Library {
  public abstract long getStartAddress(Object receiver);

  public abstract long getEndAddress(Object receiver);

  public int read(Object receiver, long address, byte[] data, int length) {
    return -1;
  }

  public int write(Object receiver, long address, byte[] data, int length) {
    return -1;
  }

  public static LibraryFactory<RegionLibrary> getFactory() {
      return FACTORY;
  }

  public static RegionLibrary getUncached() {
      return FACTORY.getUncached();
  }

  private static final LibraryFactory<RegionLibrary> FACTORY =
              LibraryFactory.resolve(RegionLibrary.class);
}