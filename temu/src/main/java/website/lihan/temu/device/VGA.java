package website.lihan.temu.device;

import static website.lihan.temu.cpu.Utils.BYTES;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

public final class VGA {
  private final int screenWidth = 400;
  private final int screenHeight = 300;

  private final long controlAddress;
  private final int controlSize = 8;
  private final long vmemAddress;
  private final int vmemSize = screenWidth * screenHeight * 4;

  private byte syncRequested = 0;
  private final byte[] vmem;
  private final WritableImage image;

  public VGA() {
    this(0xa0000100L, 0xa1000000L);
  }

  public VGA(long controlAddress, long frameBufferAddress) {
    this.controlAddress = controlAddress;
    this.vmemAddress = frameBufferAddress;

    vmem = new byte[vmemSize];
    image = new WritableImage(screenWidth, screenHeight);
  }

  public Image getImage() {
    return image;
  }

  public Control getControl() {
    return new Control();
  }

  public FrameBuffer getFrameBuffer() {
    return new FrameBuffer();
  }

  @ExportLibrary(DeviceLibrary.class)
  public class Control {
    @ExportMessage
    public long getStartAddress() {
      return controlAddress;
    }

    @ExportMessage
    public long getEndAddress() {
      return controlAddress + controlSize;
    }

    @ExportMessage
    public int read(long address, byte[] data, int length) {
      if (length != 4) {
        return -1;
      }
      switch ((int) address) {
        case 0 -> {
          // width
          BYTES.putInt(data, 0, (screenWidth << 16) | screenHeight);
          return 4;
        }
        case 4 -> {
          // height
          BYTES.putInt(data, 0, syncRequested);
          return 4;
        }
        default -> {
          return -1;
        }
      }
    }

    @ExportMessage
    public int write(long address, byte[] data, int length) {
      if (length != 4) return -1;
      if (address == 4) {
        updateScreen();
        return length;
      }
      return -1;
    }

    @TruffleBoundary
    private void updateScreen() {
      var pixelWriter = image.getPixelWriter();
      pixelWriter.setPixels(
          0,
          0,
          screenWidth,
          screenHeight,
          PixelFormat.getByteBgraInstance(),
          vmem,
          0,
          screenWidth * 4);
    }
  }

  @ExportLibrary(DeviceLibrary.class)
  public class FrameBuffer {
    @ExportMessage
    public long getStartAddress() {
      return vmemAddress;
    }

    @ExportMessage
    public long getEndAddress() {
      return vmemAddress + vmemSize;
    }

    @ExportMessage
    public int read(long address, byte[] data, int length) {
      System.arraycopy(vmem, (int) address, data, 0, length);
      return length;
    }

    @ExportMessage
    public int write(long address, byte[] data, int length) {
      System.arraycopy(data, 0, vmem, (int) address, length);
      vmem[(int) address + 3] = (byte) 0xff;
      return length;
    }
  }
}
