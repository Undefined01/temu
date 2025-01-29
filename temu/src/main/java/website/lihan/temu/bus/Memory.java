package website.lihan.temu.bus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

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
  public int read(long address, byte[] buffer, int length) {
    System.arraycopy(memory, (int)address, buffer, 0, length);
    return 0;
  }

  @Override
  public int write(long address, byte[] buffer, int length) {
    System.arraycopy(buffer, 0, memory, (int)address, length);
    return 0;
  }
}
