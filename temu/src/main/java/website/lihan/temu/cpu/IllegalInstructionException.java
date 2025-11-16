package website.lihan.temu.cpu;

import com.oracle.truffle.api.nodes.ControlFlowException;

public class IllegalInstructionException {
  /// Halt execution when an illegal instruction is encountered instead of trapping.
  /// This is useful for debugging emulator.
  public static final boolean DEBUG_ILLEGAL_INSTRUCTION = true;

  private IllegalInstructionException() {}

  public static ControlFlowException create(long pc, String format, Object... args) {
    if (DEBUG_ILLEGAL_INSTRUCTION) {
      return HaltException.create(pc, 1, format, args);
    } else {
      return InterruptException.createIllegalInstruction(pc);
    }
  }
}
