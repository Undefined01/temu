package website.lihan.temu.cpu.instr;

import website.lihan.temu.cpu.IllegalInstructionException;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.RvUtils;

public class OpImm32 {
  public static void execute(Rv64State cpu, int instr) {
    final var i = RvUtils.IInstruct.decode(instr);
    final var op1 = (int) cpu.getReg(i.rs1());
    final var op2 = i.imm();
    final var funct3 = i.funct3();
    final var funct7 = i.funct7();
    final var shamt = op2 & 0x1f;
    final var res =
        switch (funct3) {
          case 0b000 -> op1 + op2;
          case 0b001 -> op1 << shamt;
          case 0b101 ->
              switch (funct7) {
                case 0b0000000 -> op1 >>> shamt;
                case 0b0100000 -> op1 >> shamt;
                default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
              };
          case 0b110 -> op1 | op2;
          case 0b111 -> op1 & op2;
          default -> throw IllegalInstructionException.create("Invalid funct3 %d", funct3);
        };
    cpu.setReg(i.rd(), res);
  }
}
