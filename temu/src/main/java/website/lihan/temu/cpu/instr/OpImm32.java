package website.lihan.temu.cpu.instr;

import static website.lihan.temu.cpu.instr.OpImm32.Funct3.*;

import website.lihan.temu.cpu.IllegalInstructionException;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.RvUtils;

public final class OpImm32 {
  public static void execute(Rv64State cpu, long pc, int instr) {
    final var i = RvUtils.IInstruct.decode(instr);
    final var op1 = (int) cpu.getReg(i.rs1());
    final var op2 = i.imm();
    final var funct3 = i.funct3();
    final var funct7 = i.funct7();
    final var shamt = op2 & 0x1f;
    final var res =
        switch (funct3) {
          case ADD -> op1 + op2;
          case SLL -> op1 << shamt;
          case SRL ->
              switch (funct7) {
                case 0b0000000 -> op1 >>> shamt;
                case 0b0100000 -> op1 >> shamt;
                default ->
                    throw IllegalInstructionException.create(pc, "Invalid funct7 %d", funct7);
              };
          case OR -> op1 | op2;
          case AND -> op1 & op2;
          default -> throw IllegalInstructionException.create(pc, "Invalid funct3 %d", funct3);
        };
    cpu.setReg(i.rd(), res);
  }

  public static final class Funct3 {
    public static final int ADD = 0b000;
    public static final int SLL = 0b001;
    public static final int SRL = 0b101;
    public static final int OR = 0b110;
    public static final int AND = 0b111;
  }
}
