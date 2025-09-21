package website.lihan.temu.device;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@ExportLibrary(DeviceLibrary.class)
public final class Memory {
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

  @ExportMessage
  public long getStartAddress() {
    return startAddress;
  }

  @ExportMessage
  public long getEndAddress() {
    return endAddress;
  }

  @ExportMessage
  @ExplodeLoop
  public int read(long address, byte[] buffer, int length) {
    System.arraycopy(memory, (int) address, buffer, 0, length);
    return 0;
  }

  @ExportMessage
  @ExplodeLoop
  public int write(long address, byte[] buffer, int length) {
    System.arraycopy(buffer, 0, memory, (int) address, length);
    return 0;
  }
}
