package website.lihan.temu.device;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.EmulatorGUI;

public final class Bus extends Node {
  private static final InvalidDevice INVALID_DEVICE = new InvalidDevice();

  @CompilationFinal(dimensions = 1)
  private Object[] devices;

  @CompilationFinal(dimensions = 1)
  private long[] regionStartAddresses;

  @CompilationFinal(dimensions = 1)
  private long[] regionEndAddresses;

  public Bus(Object[] devices) {
    this.devices = devices;

    regionStartAddresses = new long[devices.length];
    regionEndAddresses = new long[devices.length];
    var deviceLib = DeviceLibrary.getUncached();
    for (int i = 0; i < devices.length; i++) {
      var device = devices[i];
      regionStartAddresses[i] = deviceLib.getStartAddress(device);
      regionEndAddresses[i] = deviceLib.getEndAddress(device);
    }
  }

  public int executeRead(long address, byte[] data, int length) {
    var device = findDevice(address);
    if (device != null) {
      var deviceLib = DeviceLibrary.getUncached();
      address -= deviceLib.getStartAddress(device);
      return deviceLib.read(device, address, data, length);
    }
    return -1;
  }

  public int executeWrite(long address, byte[] data, int length) {
    var device = findDevice(address);
    if (device != null) {
      var deviceLib = DeviceLibrary.getUncached();
      address -= deviceLib.getStartAddress(device);
      return deviceLib.write(device, address, data, length);
    }
    return -1;
  }

  @ExplodeLoop
  public Object findDevice(long address) {
    for (int i = 0; i < devices.length; i++) {
      if (regionStartAddresses[i] <= address && address < regionEndAddresses[i]) {
        return devices[i];
      }
    }
    return INVALID_DEVICE;
  }

  @TruffleBoundary
  public static Bus withDefault() {
    var keyboard = new Keyboard();
    var vga = new FbDev();
    var gui = new EmulatorGUI();
    gui.connect(keyboard);
    gui.connect(vga);
    gui.show();
    return new Bus(
        new Object[] {
          new Memory(),
          new RTC(),
          new SerialPort(),
          keyboard,
          vga.getControl(),
          vga.getFrameBuffer()
        });
  }
}
