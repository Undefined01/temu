package website.lihan.temu.device;

import static website.lihan.temu.cpu.RvUtils.BYTES;

import com.oracle.truffle.api.CompilerAsserts;
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
    this(0x80000000L, 0x8000000); // 128 MB
  }

  public Memory(long startAddress, int size) {
    this.startAddress = startAddress;
    this.endAddress = startAddress + size;
    this.memory = new byte[size];
  }

  public void loadFromFile(String filename) {
    CompilerAsserts.neverPartOfCompilation();
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
  public byte read1(long address) {
    return BYTES.getByte(memory, (int) address);
  }

  @ExportMessage
  public short read2(long address) {
    return BYTES.getShort(memory, (int) address);
  }

  @ExportMessage
  public int read4(long address) {
    return BYTES.getInt(memory, (int) address);
  }

  @ExportMessage
  public long read8(long address) {
    return BYTES.getLong(memory, (int) address);
  }

  @ExportMessage
  @ExplodeLoop
  public int write(long address, byte[] buffer, int length) {
    System.arraycopy(buffer, 0, memory, (int) address, length);
    return 0;
  }

  @ExportMessage
  public void write1(long address, byte value) {
    BYTES.putByte(memory, (int) address, value);
  }

  @ExportMessage
  public void write2(long address, short value) {
    BYTES.putShort(memory, (int) address, value);
  }

  @ExportMessage
  public void write4(long address, int value) {
    BYTES.putInt(memory, (int) address, value);
  }

  @ExportMessage
  public void write8(long address, long value) {
    BYTES.putLong(memory, (int) address, value);
  }
}
