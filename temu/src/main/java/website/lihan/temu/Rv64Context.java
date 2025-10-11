package website.lihan.temu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import java.util.Objects;
import website.lihan.temu.cpu.ExecPageCache;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.device.Bus;

public final class Rv64Context {
  private static final TruffleLanguage.ContextReference<Rv64Context> REF =
      TruffleLanguage.ContextReference.create(Rv64BytecodeLanguage.class);

  private final Rv64BytecodeLanguage language;

  public final Bus bus;
  public final Rv64State state;
  public final ExecPageCache execPageCache;

  public static Rv64Context get(Node node) {
    return REF.get(node);
  }

  public Rv64Context(Rv64BytecodeLanguage language, Bus bus) {
    this.language = language;
    this.state = new Rv64State();
    this.bus = bus;
    this.execPageCache = new ExecPageCache(this);
  }

  public Rv64BytecodeLanguage getLanguage() {
    return language;
  }

  public Rv64State getState() {
    return state;
  }

  public Bus getBus() {
    return bus;
  }

  public ExecPageCache getExecPageCache() {
    return execPageCache;
  }

  public Object createScopeObject() {
    var obj = new Scope();
    var objs = InteropLibrary.getUncached();
    try {
      var regs = new Object[32];
      for (int i = 0; i < 32; i++) {
        regs[i] = state.getReg(i);
      }
      objs.writeMember(obj, "registers", new TruffleList(regs));
    } catch (UnknownIdentifierException
        | UnsupportedMessageException
        | UnsupportedTypeException e) {
      throw new RuntimeException(e);
    }
    return obj;
  }
}

@ExportLibrary(InteropLibrary.class)
final class Scope extends DynamicObject {
  public Scope() {
    super(Shape.newBuilder().build());
  }

  @ExportMessage
  boolean hasLanguage() {
    return true;
  }

  @ExportMessage
  Class<? extends TruffleLanguage<?>> getLanguage() {
    return Rv64BytecodeLanguage.class;
  }

  @ExportMessage
  @TruffleBoundary
  Object toDisplayString(boolean allowSideEffects) {
    return Objects.toString(this);
  }

  @ExportMessage
  boolean isScope() {
    return true;
  }

  @ExportMessage
  boolean hasMembers() {
    return true;
  }

  @ExportMessage
  Object getMembers(
      @SuppressWarnings("unused") boolean includeInternal,
      @CachedLibrary("this") DynamicObjectLibrary objectLibrary) {
    return new TruffleList(objectLibrary.getKeyArray(this));
  }

  @ExportMessage(name = "isMemberReadable")
  @ExportMessage(name = "isMemberModifiable")
  boolean existsMember(String member, @CachedLibrary("this") DynamicObjectLibrary objectLibrary) {
    return objectLibrary.containsKey(this, member);
  }

  @ExportMessage
  boolean isMemberInsertable(String member, @CachedLibrary("this") InteropLibrary receivers) {
    return !receivers.isMemberExisting(this, member);
  }

  /** {@link DynamicObjectLibrary} provides the polymorphic inline cache for reading properties. */
  @ExportMessage
  Object readMember(String name, @CachedLibrary("this") DynamicObjectLibrary objectLibrary)
      throws UnknownIdentifierException {
    Object result = objectLibrary.getOrDefault(this, name, null);
    if (result == null) {
      /* Property does not exist. */
      throw UnknownIdentifierException.create(name);
    }
    return result;
  }

  @ExportMessage
  void writeMember(
      String name, Object value, @CachedLibrary("this") DynamicObjectLibrary objectLibrary) {
    objectLibrary.put(this, name, value);
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
