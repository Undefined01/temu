package website.lihan.temu.device;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.memory.ByteArraySupport;
import java.util.LinkedList;
import java.util.Queue;
import javafx.scene.input.KeyCode;

@ExportLibrary(DeviceLibrary.class)
public final class Keyboard {
  public final long baseAddress;

  private static final ByteArraySupport BYTES = ByteArraySupport.littleEndian();
  private static final int KEYDOWN_MASK = 0x8000;
  private static final int KEY_QUEUE_LEN = 1024;
  private final Queue<Integer> keyQueue = new LinkedList<>();

  public Keyboard() {
    this(0xa0000060L);
  }

  public Keyboard(long baseAddress) {
    this.baseAddress = baseAddress;
  }

  @ExportMessage
  public long getStartAddress() {
    return baseAddress;
  }

  @ExportMessage
  public long getEndAddress() {
    return baseAddress + 4;
  }

  @ExportMessage
  public int read(long address, byte[] data, int length) {
    if (length > 4) return -1;
    var key = keyQueue.isEmpty() ? NemuKeyCode.NONE.code : keyQueue.poll();
    byte[] temp = new byte[4];
    BYTES.putInt(temp, 0, key);
    System.arraycopy(temp, 0, data, 0, length);
    return length;
  }

  @ExportMessage
  public int write(long address, byte[] data, int length) {
    return -1;
  }

  public void sendKey(KeyCode keyCode, boolean isKeydown) {
    var nemuKey = NemuKeyCode.fromJavaFXKeyCode(keyCode);
    if (nemuKey != NemuKeyCode.NONE) {
      int amScancode = nemuKey.code | (isKeydown ? KEYDOWN_MASK : 0);
      if (keyQueue.size() < KEY_QUEUE_LEN) {
        keyQueue.add(amScancode);
      }
    }
  }
}
