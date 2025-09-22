package website.lihan.temu.device;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.Node.Children;
import website.lihan.temu.EmulatorGUI;
import website.lihan.temu.Utils;

public final class Bus extends Node {
  @CompilationFinal(dimensions = 1)
  private Object[] regions;

  @Children private DeviceLibrary[] regionLibraries;

  public Bus(Object[] regions) {
    this.regions = regions;
    this.regionLibraries = new DeviceLibrary[regions.length];
    for (int i = 0; i < regions.length; i++) {
      this.regionLibraries[i] = DeviceLibrary.getFactory().create(regions[i]);
    }
  }

  @TruffleBoundary
  public int executeRead(long address, byte[] data, int length) {
    for (int i = 0; i < regions.length; i++) {
      var region = regions[i];
      var regionLib = regionLibraries[i];
      if (regionLib.getStartAddress(region) <= address
          && address < regionLib.getEndAddress(region)) {
        address -= regionLib.getStartAddress(region);
        return regionLib.read(region, address, data, length);
      }
    }
    Utils.printf("[Bus] read from invalid address: 0x%08x\n", address);
    return -1;
  }

  @TruffleBoundary
  public int executeWrite(long address, byte[] data, int length) {
    var regionIdx = findRegion(address);
    if (regionIdx < 0) {
      Utils.printf("[Bus] write to invalid address: 0x%08x\n", address);
      return -1;
    }
    var region = regions[regionIdx];
    var regionLib = regionLibraries[regionIdx];
    address -= regionLib.getStartAddress(region);
    return regionLib.write(region, address, data, length);
  }

  @ExplodeLoop
  private int findRegion(long address) {
    for (int i = 0; i < regions.length; i++) {
      var region = regions[i];
      var regionLib = regionLibraries[i];
      if (regionLib.getStartAddress(region) <= address
          && address < regionLib.getEndAddress(region)) {
        return i;
      }
    }
    return -1;
  }

  @TruffleBoundary
  public static Bus withDefault() {
    var keyboard = new Keyboard();
    var vga = new VGA();
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
