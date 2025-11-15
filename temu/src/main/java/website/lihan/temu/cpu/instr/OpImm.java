package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.CompilerDirectives;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.RvUtils;

public class OpImm {
  public static void execute(Rv64State cpu, int instr) {
    final var i = RvUtils.IInstruct.decode(instr);
    final var op1 = cpu.getReg(i.rs1());
    final var op2 = (long) i.imm();
    final var funct3 = i.funct3();
    final var funct7 = i.funct7();
    final var shamt = i.imm() & 0x3f;
    final var res =
        switch (funct3) {
          case 0b000 -> op1 + op2;
          case 0b001 -> op1 << shamt;
          case 0b010 -> op1 < op2 ? 1 : 0;
          case 0b011 -> Long.compareUnsigned(op1, op2) < 0 ? 1 : 0;
          case 0b100 -> op1 ^ op2;
          case 0b101 ->
              switch (i.funct7() >> 1) {
                case 0b000000 -> op1 >>> shamt;
                case 0b010000 -> op1 >> shamt;
                default -> throw CompilerDirectives.shouldNotReachHere();
              };
          case 0b110 -> op1 | op2;
          case 0b111 -> op1 & op2;
          default -> throw CompilerDirectives.shouldNotReachHere();
        };
    cpu.setReg(i.rd(), res);
  }
}
