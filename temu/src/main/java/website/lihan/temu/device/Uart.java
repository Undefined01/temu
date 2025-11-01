package website.lihan.temu.device;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import java.util.LinkedList;
import java.util.Queue;

@ExportLibrary(DeviceLibrary.class)
public final class Uart {
  private static final long BASE_ADDRESS = 0x10000000L;
  
  // UART register offsets
  private static final int RHR = 0; // Receiver Holding Register (read)
  private static final int THR = 0; // Transmitter Holding Register (write)
  private static final int IER = 1; // Interrupt Enable Register
  private static final int ISR = 2; // Interrupt Status Register (read) / FCR (write)
  private static final int LCR = 3; // Line Control Register
  private static final int MCR = 4; // Modem Control Register
  private static final int LSR = 5; // Line Status Register
  private static final int MSR = 6; // Modem Status Register
  private static final int SCR = 7; // Scratch Register
  
  // IER bits
  private static final byte IER_RX_ENABLE = (byte) 0x01;
  private static final byte IER_TX_ENABLE = (byte) 0x02;
  
  // ISR bits
  private static final byte ISR_NO_INT = (byte) 0x01;
  private static final byte ISR_TX_EMPTY = (byte) 0x02;
  private static final byte ISR_RX_AVAIL = (byte) 0x04;
  
  // LSR flags
  private static final byte LSR_RX_READY = (byte) 0x01; // Data ready
  private static final byte LSR_TX_EMPTY = (byte) 0x20; // THR empty
  private static final byte LSR_TX_IDLE = (byte) 0x40;  // THR empty and line idle
  
  // LCR bits
  private static final byte LCR_DLAB = (byte) 0x80; // Divisor Latch Access Bit
  
  private final Queue<Byte> rxQueue = new LinkedList<>();
  private byte ier = 0;
  private byte lcr = 0;
  private byte mcr = 0;
  private byte scr = 0;
  private byte divisorLow = 0;
  private byte divisorHigh = 0;
  
  // Interrupt state
  private boolean txInterruptPending = false;
  private boolean rxInterruptPending = false;
  
  @ExportMessage
  public long getStartAddress() {
    return BASE_ADDRESS;
  }

  @ExportMessage
  public long getEndAddress() {
    return BASE_ADDRESS + 8;
  }

  @ExportMessage
  public byte read1(long address) {
    int offset = (int) (address - BASE_ADDRESS);
    switch (offset) {
      case RHR:
        // When DLAB is set, this is divisor latch low byte
        if ((lcr & LCR_DLAB) != 0) {
          return divisorLow;
        }
        // Otherwise, read from receive buffer
        if (!rxQueue.isEmpty()) {
          byte data = rxQueue.poll();
          // Clear RX interrupt when buffer becomes empty
          if (rxQueue.isEmpty()) {
            rxInterruptPending = false;
          }
          return data;
        }
        return 0;
      case IER:
        // When DLAB is set, this is divisor latch high byte
        if ((lcr & LCR_DLAB) != 0) {
          return divisorHigh;
        }
        return ier;
      case ISR:
        return getInterruptStatus();
      case LCR:
        return lcr;
      case MCR:
        return mcr;
      case LSR:
        byte lsr = (byte) (LSR_TX_EMPTY | LSR_TX_IDLE); // Transmitter always ready
        if (!rxQueue.isEmpty()) {
          lsr |= LSR_RX_READY;
        }
        return lsr;
      case MSR:
        return (byte) 0xB0; // CTS, DSR, and CD all set
      case SCR:
        return scr;
      default:
        return 0;
    }
  }

  @ExportMessage
  public int read4(long address) {
    return read1(address) & 0xFF;
  }

  @ExportMessage
  public int read(long address, byte[] data, int length) {
    for (int i = 0; i < length; i++) {
      data[i] = read1(address + i);
    }
    return length;
  }

  @ExportMessage
  @TruffleBoundary
  public void write1(long address, byte value) {
    int offset = (int) (address - BASE_ADDRESS);
    switch (offset) {
      case THR:
        // When DLAB is set, this is divisor latch low byte
        if ((lcr & LCR_DLAB) != 0) {
          divisorLow = value;
        } else {
          // Transmit the character
          System.out.print((char) value);
          System.out.flush();
          // Set TX interrupt if enabled
          if ((ier & IER_TX_ENABLE) != 0) {
            txInterruptPending = true;
          }
        }
        break;
      case IER:
        // When DLAB is set, this is divisor latch high byte
        if ((lcr & LCR_DLAB) != 0) {
          divisorHigh = value;
        } else {
          ier = (byte) (value & 0x0F); // Only lower 4 bits used
          // Update interrupt state based on new IER
          updateInterrupts();
        }
        break;
      case ISR:
        // This is FCR (FIFO Control Register) on write, typically ignored in simple impl
        break;
      case LCR:
        lcr = value;
        break;
      case MCR:
        mcr = value;
        break;
      case SCR:
        scr = value;
        break;
      default:
        // Ignore writes to read-only or unimplemented registers
        break;
    }
  }

  @ExportMessage
  @TruffleBoundary
  public void write4(long address, int value) {
    write1(address, (byte) value);
  }

  @ExportMessage
  public int write(long address, byte[] data, int length) {
    for (int i = 0; i < length; i++) {
      write1(address + i, data[i]);
    }
    return length;
  }
  
  /**
   * Get the current interrupt status register value
   */
  private byte getInterruptStatus() {
    // Priority: RX > TX (standard UART priority)
    if (rxInterruptPending && (ier & IER_RX_ENABLE) != 0) {
      return ISR_RX_AVAIL;
    }
    if (txInterruptPending && (ier & IER_TX_ENABLE) != 0) {
      // Reading ISR clears TX interrupt
      txInterruptPending = false;
      return ISR_TX_EMPTY;
    }
    return ISR_NO_INT;
  }
  
  /**
   * Update interrupt pending flags based on current state
   */
  private void updateInterrupts() {
    // Set TX interrupt if enabled and transmitter is idle
    if ((ier & IER_TX_ENABLE) != 0) {
      txInterruptPending = true;
    }
    // RX interrupt is already set when data arrives
  }
  
  /**
   * Check if an interrupt is pending
   * This should be called by the interrupt controller
   */
  public boolean isInterruptPending() {
    if ((rxInterruptPending && (ier & IER_RX_ENABLE) != 0) ||
        (txInterruptPending && (ier & IER_TX_ENABLE) != 0)) {
      return true;
    }
    return false;
  }
  
  /**
   * Add a byte to the receive queue (for input simulation)
   * This sets the RX interrupt pending flag
   */
  @TruffleBoundary
  public void receiveData(byte data) {
    rxQueue.offer(data);
    if ((ier & IER_RX_ENABLE) != 0) {
      rxInterruptPending = true;
    }
  }
  
  /**
   * Receive multiple bytes (for convenience)
   */
  @TruffleBoundary
  public void receiveString(String str) {
    for (char c : str.toCharArray()) {
      receiveData((byte) c);
    }
  }
}
