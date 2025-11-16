package website.lihan.temu.cpu;

import com.oracle.truffle.api.nodes.ControlFlowException;
import website.lihan.temu.Configuration;

public class IllegalInstructionException {
  private IllegalInstructionException() {}

  public static ControlFlowException create(long pc, String format, Object... args) {
    if (Configuration.haltAtIllegalInstruction) {
      return HaltException.create(pc, 1, format, args);
    } else {
      return InterruptException.createIllegalInstruction(pc);
    }
  }
}
