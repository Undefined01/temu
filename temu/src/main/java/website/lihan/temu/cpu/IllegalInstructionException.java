package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ControlFlowException;

public class IllegalInstructionException extends ControlFlowException {
  private long pc = 0;
  private int instr = 0;

  public IllegalInstructionException() {
    super();
  }

  @TruffleBoundary
  public static IllegalInstructionException create() {
    return new IllegalInstructionException();
  }

  @TruffleBoundary
  public static IllegalInstructionException create(long pc, int instr) {
    var exception = new IllegalInstructionException();
    exception.pc = pc;
    exception.instr = instr;
    return exception;
  }

  @Override
  public String getMessage() {
    return "Illegal instruction at " + Long.toHexString(pc) + ": " + Integer.toHexString(instr);
  }
}
