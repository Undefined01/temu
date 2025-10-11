package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.CompilerDirectives;
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.IllegalInstructionException;
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
    final var res =
        switch (funct3) {
          case 0b000 ->
              switch (funct7) {
                case 0b0000000 -> op1 + op2;
                case 0b0100000 -> op1 - op2;
                case 0b0000001 -> op1 * op2;
                default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
              };
          case 0b001 ->
              switch (funct7) {
                case 0b0000000 -> op1 << shamt;
                case 0b0000001 -> Math.multiplyHigh(op1, op2);
                default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
              };
          case 0b010 ->
              switch (funct7) {
                case 0b0000000 -> op1 < op2 ? 1 : 0;
                case 0b0000001 -> Utils.signedUnsignedMultiplyHigh(op1, op2);
                default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
              };
          case 0b011 ->
              switch (funct7) {
                case 0b0000000 -> Long.compareUnsigned(op1, op2) < 0 ? 1 : 0;
                case 0b0000001 -> Math.unsignedMultiplyHigh(op1, op2);
                default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
              };
          case 0b100 ->
              switch (funct7) {
                case 0b0000000 -> op1 ^ op2;
                case 0b0000001 -> {
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
                default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
              };
          case 0b101 ->
              switch (funct7) {
                case 0b0000000 -> op1 >>> shamt;
                case 0b0100000 -> op1 >> shamt;
                case 0b0000001 -> op2 != 0 ? Long.divideUnsigned(op1, op2) : -1;
                default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
              };
          case 0b110 ->
              switch (funct7) {
                case 0b0000000 -> op1 | op2;
                case 0b0000001 -> {
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
                default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
              };
          case 0b111 ->
              switch (funct7) {
                case 0b0000000 -> op1 & op2;
                case 0b0000001 -> op2 != 0 ? Long.remainderUnsigned(op1, op2) : op1;
                default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
              };
          default -> throw CompilerDirectives.shouldNotReachHere();
        };
    cpu.setReg(r.rd(), res);
  }
}
