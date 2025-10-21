package website.lihan.temu.cpu.csr;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;

@GenerateLibrary
public abstract class CsrLibrary extends Library {
  public abstract long getValue(Object receiver);

  public abstract void setValue(Object receiver, long newValue);

  public static final LibraryFactory<CsrLibrary> FACTORY = LibraryFactory.resolve(CsrLibrary.class);

  public static LibraryFactory<CsrLibrary> getFactory() {
    return FACTORY;
  }
}
