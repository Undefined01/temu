package website.lihan.temu.bus;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Utils;

public final class Bus extends Node {
  @CompilationFinal(dimensions = 1)
  private Region[] regions;

  public Bus(Region[] regions) {
    this.regions = regions;
  }

  public int executeRead(long address, byte[] data, int length) {
    var region = findRegion(address);
    if (region == null) {
      Utils.printf("[Bus] read from invalid address: 0x%08x\n", address);
      return -1;
    }
    address -= region.getStartAddress();
    return region.read(address, data, length);
  }

  public int executeWrite(long address, byte[] data, int length) {
    var region = findRegion(address);
    if (region == null) {
      Utils.printf("[Bus] write to invalid address: 0x%08x\n", address);
      return -1;
    }
    address -= region.getStartAddress();
    return region.write(address, data, length);
  }

  @ExplodeLoop
  private Region findRegion(long address) {
    for (Region region : regions) {
      if (region.getStartAddress() <= address && address < region.getEndAddress()) {
        return region;
      }
    }
    return null;
  }
}
