package website.lihan.temu.cpu.instr;

import static website.lihan.temu.cpu.instr.Op.Funct10.*;

import com.oracle.truffle.api.CompilerDirectives;
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.RvUtils;

public class Op {
  public static void execute(Rv64State cpu, int instr) {
    final var r = RvUtils.RInstruct.decode(instr);
    final var op1 = cpu.getReg(r.rs1());
    final var op2 = cpu.getReg(r.rs2());
    final var funct3 = r.funct3();
    final var funct7 = r.funct7();
    final var shamt = op2 & 0x3f;
    final var funct10 = (funct7 << 3) | funct3;
    final var res =
        switch (funct10) {
          // RV64I
          case ADD -> op1 + op2;
          case SUB -> op1 - op2;
          case SHL -> op1 << shamt;
          case SHR -> op1 >>> shamt;
          case SRA -> op1 >> shamt;
          case SLT -> op1 < op2 ? 1 : 0;
          case SLTU -> Long.compareUnsigned(op1, op2) < 0 ? 1 : 0;
          case XOR -> op1 ^ op2;
          case OR -> op1 | op2;
          case AND -> op1 & op2;

          // RV64M
          case MUL -> op1 * op2;
          case MULH -> Math.multiplyHigh(op1, op2);
          case MULHSU -> Utils.signedUnsignedMultiplyHigh(op1, op2);
          case MULHU -> Math.unsignedMultiplyHigh(op1, op2);
          case DIV -> {
            if (op2 == 0) {
              // Division by zero
              yield -1;
            }
            if (op1 == Long.MIN_VALUE && op2 == -1) {
              // Overflow
              yield op1;
            }
            yield op1 / op2;
          }
          case DIVU -> op2 != 0 ? Long.divideUnsigned(op1, op2) : -1;
          case REM -> {
            if (op2 == 0) {
              // Division by zero
              yield op1;
            }
            if (op1 == Long.MIN_VALUE && op2 == -1) {
              // Overflow
              yield 0;
            }
            yield op1 % op2;
          }
          case REMU -> op2 != 0 ? Long.remainderUnsigned(op1, op2) : op1;

          default -> throw CompilerDirectives.shouldNotReachHere();
        };
    cpu.setReg(r.rd(), res);
  }

  /// funct7 [31:25] + funct3 [14:12] as 10 bits
  public static class Funct10 {
    // RV64I
    public static final int ADD = 0b0000000_000;
    public static final int SUB = 0b0100000_000;
    public static final int SHL = 0b0000000_001;
    public static final int SHR = 0b0000000_101;
    public static final int SRA = 0b0100000_101;
    public static final int SLT = 0b0000000_010;
    public static final int SLTU = 0b0000000_011;
    public static final int XOR = 0b0000000_100;
    public static final int OR = 0b0000000_110;
    public static final int AND = 0b0000000_111;

    // RV64M
    public static final int MUL = 0b0000001_000;
    public static final int MULH = 0b0000001_001;
    public static final int MULHSU = 0b0000001_010;
    public static final int MULHU = 0b0000001_011;
    public static final int DIV = 0b0000001_100;
    public static final int DIVU = 0b0000001_101;
    public static final int REM = 0b0000001_110;
    public static final int REMU = 0b0000001_111;
  }
}
