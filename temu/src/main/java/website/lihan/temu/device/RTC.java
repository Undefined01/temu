package website.lihan.temu.device;

import static website.lihan.temu.cpu.RvUtils.BYTES;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import java.util.Timer;
import java.util.TimerTask;

@ExportLibrary(DeviceLibrary.class)
public final class RTC {
  private final long baseAddress;

  private long mTimeCmp = Integer.MAX_VALUE;

  private Timer timer = new Timer();
  private TimerTask task = null;
  private static boolean interrupt = false;

  public RTC() {
    this(0xa0000048L);
  }

  public RTC(long baseAddress) {
    this.baseAddress = baseAddress;
  }

  @ExportMessage
  public long getStartAddress() {
    return baseAddress;
  }

  @ExportMessage
  public long getEndAddress() {
    return baseAddress + 16;
  }

  @ExportMessage
  public int read(long address, byte[] data, int length) {
    if (length > 8) return -1;

    switch ((int) address) {
      case 0:
        {
          long microseconds = getTime();
          byte[] temp = new byte[8];
          BYTES.putLong(temp, 0, microseconds);
          System.arraycopy(temp, 0, data, 0, length);
          return length;
        }
      case 8:
        {
          byte[] temp = new byte[8];
          BYTES.putLong(temp, 0, mTimeCmp);
          System.arraycopy(temp, 0, data, 0, length);
          return length;
        }
    }
    return -1;
  }

  @ExportMessage
  public long read8(long address) {
    switch ((int) address) {
      case 0:
        {
          return getTime();
        }
      case 8:
        {
          return mTimeCmp;
        }
      default:
        return -1;
    }
  }

  @ExportMessage
  public int write(long address, byte[] data, int length) {
    if (length > 8) return -1;

    switch ((int) address) {
      case 8:
        {
          byte[] temp = new byte[8];
          System.arraycopy(data, 0, temp, 0, length);
          mTimeCmp = BYTES.getLong(temp, 0);
          return length;
        }
    }
    return -1;
  }

  @ExportMessage
  public void write8(long address, long value) {
    switch ((int) address) {
      case 8:
        {
          mTimeCmp = value;
          scheduleTimer();
          break;
        }
    }
  }

  @TruffleBoundary
  public static long getTime() {
    return System.currentTimeMillis() * 1000;
  }

  @TruffleBoundary
  private void scheduleTimer() {
    interrupt = false;
    if (task != null) {
      task.cancel();
    }
    long delay = mTimeCmp - getTime();
    if (delay < 0) {
      delay = 0;
    }
    task =
        new TimerTask() {
          @Override
          public void run() {
            if (getTime() < mTimeCmp) {
              // wake up early, reschedule
              scheduleTimer();
              return;
            }

            RTC.interrupt = true;
          }
        };
    timer.schedule(task, Math.max(delay / 1000, 1));
  }

  public static boolean checkInterrupt() {
    return interrupt;
  }
}
