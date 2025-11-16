package website.lihan.temu.cpu.instr;

import static website.lihan.temu.cpu.instr.OpImm.Funct3.*;

import com.oracle.truffle.api.CompilerDirectives;
import website.lihan.temu.cpu.IllegalInstructionException;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.RvUtils;

public class OpImm {
  public static void execute(Rv64State cpu, long pc, int instr) {
    final var i = RvUtils.IInstruct.decode(instr);
    final var op1 = cpu.getReg(i.rs1());
    final var op2 = (long) i.imm();
    final var funct3 = i.funct3();
    final var funct7 = i.funct7();
    final var shamt = i.imm() & 0x3f;
    final var res =
        switch (funct3) {
          case ADD -> op1 + op2;
          case SLL -> op1 << shamt;
          case SLT -> op1 < op2 ? 1 : 0;
          case SLTU -> Long.compareUnsigned(op1, op2) < 0 ? 1 : 0;
          case XOR -> op1 ^ op2;
          case SRL ->
              switch (funct7 >> 1) {
                case 0b000000 -> op1 >>> shamt;
                case 0b010000 -> op1 >> shamt;
                default ->
                    throw IllegalInstructionException.create(pc, "Invalid funct3 %d", funct3);
              };
          case OR -> op1 | op2;
          case AND -> op1 & op2;
          default -> throw CompilerDirectives.shouldNotReachHere();
        };
    cpu.setReg(i.rd(), res);
  }

  public static final class Funct3 {
    public static final int ADD = 0b000;
    public static final int SLL = 0b001;
    public static final int SLT = 0b010;
    public static final int SLTU = 0b011;
    public static final int XOR = 0b100;
    public static final int SRL = 0b101;
    public static final int OR = 0b110;
    public static final int AND = 0b111;
  }
}
