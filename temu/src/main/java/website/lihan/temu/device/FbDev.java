package website.lihan.temu.device;

import static website.lihan.temu.cpu.RvUtils.BYTES;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

/// A simple framebuffer device with control registers and a pixel buffer.
/// Control registers:
/// 0x00: width (16 bits)
/// 0x02: height (16 bits)
/// 0x04: sync request (write 1 to request a screen update)
/// Framebuffer:
/// Pixels are stored in BGRA format, 4 bytes per pixel, in row-major order.
public final class FbDev {
  private final int screenWidth = 400;
  private final int screenHeight = 300;

  private final long controlAddress;
  private final int controlSize = 8;
  private final long vmemAddress;
  private final int vmemSize = screenWidth * screenHeight * 4;

  private byte syncRequested = 0;
  private final byte[] vmem;
  private final WritableImage image;

  public FbDev() {
    this(0xa0000100L, 0xa1000000L);
  }

  public FbDev(long controlAddress, long frameBufferAddress) {
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
          BYTES.putInt(data, 0, (screenWidth << 16) | screenHeight);
          return 4;
        }
        case 4 -> {
          BYTES.putInt(data, 0, syncRequested);
          return 4;
        }
        default -> {
          return -1;
        }
      }
    }

    @ExportMessage
    public short read2(long address) {
      switch ((int) address) {
        case 0 -> {
          return (short) screenHeight;
        }
        case 2 -> {
          return (short) screenWidth;
        }
        default -> {
          return -1;
        }
      }
    }

    @ExportMessage
    public int read4(long address) {
      switch ((int) address) {
        case 0 -> {
          return (screenWidth << 16) | screenHeight;
        }
        case 4 -> {
          return syncRequested;
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
      for (int i = 3; i < length; i+=4) {
        vmem[(int) address + i] |= 0xFF;
      }
      return length;
    }

    @ExportMessage
    public void write4(long address, int value) {
      value |= 0xFF000000;
      BYTES.putInt(vmem, (int) address, value);
    }

    @ExportMessage
    public void write8(long address, long value) {
      value |= 0xFF000000FF000000L;
      BYTES.putLong(vmem, (int) address, value);
    }
  }
}
