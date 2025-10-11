package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.HostCompilerDirectives;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.cpu.IllegalInstructionException;

public class BranchNode extends Node {
  public final int funct3;
  public final int rs1;
  public final int rs2;
  public final long targetPc;

  @CompilationFinal private int trueCount = 0;
  @CompilationFinal private int falseCount = 0;

  public BranchNode(int funct3, int rs1, int rs2, long targetPc) {
    this.funct3 = funct3;
    this.rs1 = rs1;
    this.rs2 = rs2;
    this.targetPc = targetPc;
  }

  public boolean condition(long op1, long op2) {
    return switch (funct3) {
      case 0b000 -> op1 == op2;
      case 0b001 -> op1 != op2;
      case 0b100 -> op1 < op2;
      case 0b101 -> op1 >= op2;
      case 0b110 -> Long.compareUnsigned(op1, op2) < 0;
      case 0b111 -> Long.compareUnsigned(op1, op2) >= 0;
      default -> throw IllegalInstructionException.create("Invalid funct3 %d", funct3);
    };
  }

  public boolean profileBranch(boolean condition) {
    if (HostCompilerDirectives.inInterpreterFastPath()) {
      if (condition) {
        if (trueCount == 0) {
          CompilerDirectives.transferToInterpreterAndInvalidate();
        }
        try {
          trueCount = Math.addExact(trueCount, 1);
        } catch (ArithmeticException e) {
          // shift count but never make it go to 0
          falseCount = (falseCount & 0x1) + (falseCount >> 1);
          trueCount = Integer.MAX_VALUE >> 1;
        }
      } else {
        if (falseCount == 0) {
          CompilerDirectives.transferToInterpreterAndInvalidate();
        }
        try {
          falseCount = Math.addExact(falseCount, 1);
        } catch (ArithmeticException e) {
          // shift count but never make it go to 0
          trueCount = (trueCount & 0x1) + (trueCount >> 1);
          falseCount = Integer.MAX_VALUE >> 1;
        }
      }
      return condition;
    } else {
      if (condition) {
        if (trueCount == 0) {
          CompilerDirectives.transferToInterpreterAndInvalidate();
        }
        if (falseCount == 0) {
          return true;
        }
      } else {
        if (falseCount == 0) {
          CompilerDirectives.transferToInterpreterAndInvalidate();
        }
        if (trueCount == 0) {
          return false;
        }
      }
      return CompilerDirectives.injectBranchProbability(
          (double) trueCount / (double) (trueCount + falseCount), condition);
    }
  }
}
