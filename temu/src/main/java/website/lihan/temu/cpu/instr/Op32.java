package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.CompilerDirectives;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.RvUtils;

public class Op32 {
  public static void execute(Rv64State cpu, int instr) {
    final var r = RvUtils.RInstruct.decode(instr);
    final int op1 = (int) cpu.getReg(r.rs1());
    final int op2 = (int) cpu.getReg(r.rs2());
    final int shamt = op2 & 0x1f;
    final int funct10 = (r.funct7() << 3) | r.funct3();

    final int res =
        switch (funct10) {
          // RV64I-W
          case Funct10.ADDW -> op1 + op2;
          case Funct10.SUBW -> op1 - op2;
          case Funct10.SLLW -> op1 << shamt;
          case Funct10.SRLW -> op1 >>> shamt;
          case Funct10.SRAW -> op1 >> shamt;

          // RV64M-W
          case Funct10.MULW -> op1 * op2;
          case Funct10.DIVW -> {
            if (op2 == 0) {
              yield -1;
            }
            if (op1 == Integer.MIN_VALUE && op2 == -1) {
              yield op1; // overflow
            }
            yield op1 / op2;
          }
          case Funct10.DIVUW -> op2 != 0 ? Integer.divideUnsigned(op1, op2) : -1;
          case Funct10.REMW -> {
            if (op2 == 0) {
              yield op1;
            }
            if (op1 == Integer.MIN_VALUE && op2 == -1) {
              yield 0; // overflow
            }
            yield op1 % op2;
          }
          case Funct10.REMUW -> op2 != 0 ? Integer.remainderUnsigned(op1, op2) : op1;

          default -> throw CompilerDirectives.shouldNotReachHere();
        };

    // Sign-extend 32-bit result to 64-bit
    cpu.setReg(r.rd(), (long) res);
  }

  /// funct7 [31:25] + funct3 [14:12] as 10 bits
  public static final class Funct10 {
    // RV64I-W
    public static final int ADDW = 0b0000000_000;
    public static final int SUBW = 0b0100000_000;
    public static final int SLLW = 0b0000000_001;
    public static final int SRLW = 0b0000000_101;
    public static final int SRAW = 0b0100000_101;

    // RV64M-W
    public static final int MULW = 0b0000001_000;
    public static final int DIVW = 0b0000001_100;
    public static final int DIVUW = 0b0000001_101;
    public static final int REMW = 0b0000001_110;
    public static final int REMUW = 0b0000001_111;

    private Funct10() {}
  }
}
