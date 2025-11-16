package website.lihan.temu.device;

import static website.lihan.temu.cpu.RvUtils.BYTES;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;

@GenerateLibrary
public abstract class DeviceLibrary extends Library {
  public abstract long getStartAddress(Object receiver);

  public abstract long getEndAddress(Object receiver);

  public int read(Object receiver, long address, byte[] data, int length) {
    return -1;
  }

  public int write(Object receiver, long address, byte[] data, int length) {
    return -1;
  }

  public byte read1(Object receiver, long address) {
    var data = new byte[1];
    read(receiver, address, data, 1);
    return BYTES.getByte(data, 0);
  }

  public short read2(Object receiver, long address) {
    var data = new byte[2];
    read(receiver, address, data, 2);
    return BYTES.getShort(data, 0);
  }

  public int read4(Object receiver, long address) {
    var data = new byte[4];
    read(receiver, address, data, 4);
    return BYTES.getInt(data, 0);
  }

  public long read8(Object receiver, long address) {
    var data = new byte[8];
    read(receiver, address, data, 8);
    return BYTES.getLong(data, 0);
  }

  public void write1(Object receiver, long address, byte value) {
    var data = new byte[1];
    BYTES.putByte(data, 0, value);
    write(receiver, address, data, 1);
  }

  public void write2(Object receiver, long address, short value) {
    var data = new byte[2];
    BYTES.putShort(data, 0, value);
    write(receiver, address, data, 2);
  }

  public void write4(Object receiver, long address, int value) {
    var data = new byte[4];
    BYTES.putInt(data, 0, value);
    write(receiver, address, data, 4);
  }

  public void write8(Object receiver, long address, long value) {
    var data = new byte[8];
    BYTES.putLong(data, 0, value);
    write(receiver, address, data, 8);
  }

  public static LibraryFactory<DeviceLibrary> getFactory() {
    return FACTORY;
  }

  public static DeviceLibrary getUncached() {
    return FACTORY.getUncached();
  }

  public static DeviceLibrary create(Object device, boolean cached) {
    if (cached) {
      return getFactory().create(device);
    } else {
      return getFactory().getUncached();
    }
  }

  private static final LibraryFactory<DeviceLibrary> FACTORY =
      LibraryFactory.resolve(DeviceLibrary.class);
}
