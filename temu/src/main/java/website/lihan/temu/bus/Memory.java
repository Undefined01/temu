package website.lihan.temu.bus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public final class Memory implements Region {
  private final long startAddress;
  private final long endAddress;
  private final byte[] memory;

  public Memory() {
    this(0x80000000L, 128 * 1024 * 1024);
  }

  public Memory(long startAddress, int size) {
    this.startAddress = startAddress;
    this.endAddress = startAddress + size;
    this.memory = new byte[size];
  }

  @TruffleBoundary
  public void loadFromFile(String filename) {
    var file = new File(filename);
    try (var fis = new FileInputStream(file)) {
      fis.read(memory);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long getStartAddress() {
    return startAddress;
  }

  @Override
  public long getEndAddress() {
    return endAddress;
  }

  @Override
  @ExplodeLoop
  public int read(long address, byte[] buffer, int length) {
    for (int i = 0; i < length; i++) {
      buffer[i] = memory[(int) (address + i)];
    }
    return 0;
  }

  @Override
  @ExplodeLoop
  public int write(long address, byte[] buffer, int length) {
    for (int i = 0; i < length; i++) {
      memory[(int) (address + i)] = buffer[i];
    }
    return 0;
  }
}
