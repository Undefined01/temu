package website.lihan.temu;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import website.lihan.temu.device.Bus;

final class Rv64Scope implements TruffleObject {
  public Bus bus;
  public Rv64Context context;

  public Rv64Scope(Rv64Context context) {
    this.context = context;
    this.bus = context.bus;
  }
}

@ExportLibrary(InteropLibrary.class)
final class TruffleList implements TruffleObject {

  private final Object[] keys;

  TruffleList(Object[] keys) {
    this.keys = keys;
  }

  @ExportMessage
  Object readArrayElement(long index) throws InvalidArrayIndexException {
    if (!isArrayElementReadable(index)) {
      throw InvalidArrayIndexException.create(index);
    }
    return keys[(int) index];
  }

  @ExportMessage
  boolean hasArrayElements() {
    return true;
  }

  @ExportMessage
  long getArraySize() {
    return keys.length;
  }

  @ExportMessage
  boolean isArrayElementReadable(long index) {
    return index >= 0 && index < keys.length;
  }
}
