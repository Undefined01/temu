package website.lihan.temu.cpu;

import com.oracle.truffle.api.memory.ByteArraySupport;

public class RvUtils {
  public static final ByteArraySupport BYTES = ByteArraySupport.littleEndian();

  public static long signExtend(long value, int bits) {
    return (value << (64 - bits)) >> (64 - bits);
  }

  public static int signExtend32(int value, int bits) {
    return (value << (32 - bits)) >> (32 - bits);
  }

  public static record IInstruct(int opcode, int rd, int rs1, int funct3, int funct7, int imm) {
    public static IInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var rd = (instr >> 7) & 0x1f;
      var funct3 = (instr >> 12) & 0x7;
      var funct7 = (instr >> 25) & 0x7f;
      var rs1 = (instr >> 15) & 0x1f;
      var imm = signExtend32(instr >> 20, 12);
      return new IInstruct(opcode, rd, rs1, funct3, funct7, imm);
    }
  }

  public static record RInstruct(int opcode, int rd, int rs1, int rs2, int funct3, int funct7) {
    public static RInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var rd = (instr >> 7) & 0x1f;
      var funct3 = (instr >> 12) & 0x7;
      var rs1 = (instr >> 15) & 0x1f;
      var rs2 = (instr >> 20) & 0x1f;
      var funct7 = (instr >> 25);
      return new RInstruct(opcode, rd, rs1, rs2, funct3, funct7);
    }
  }

  public static record UInstruct(int opcode, int rd, int imm) {
    public static UInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var rd = (instr >> 7) & 0x1f;
      var imm = (instr >> 12) << 12;
      return new UInstruct(opcode, rd, imm);
    }
  }

  public static record SInstruct(int opcode, int rs1, int rs2, int funct3, int imm) {
    public static SInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var imm11_5 = ((instr >> 25) & 0x7f) << 5;
      var imm4_0 = (instr >> 7) & 0x1f;
      var funct3 = (instr >> 12) & 0x7;
      var rs1 = (instr >> 15) & 0x1f;
      var rs2 = (instr >> 20) & 0x1f;
      var imm = signExtend32(imm11_5 | imm4_0, 12);
      return new SInstruct(opcode, rs1, rs2, funct3, imm);
    }
  }

  public static record BInstruct(int opcode, int rs1, int rs2, int funct3, int imm) {
    public static BInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var funct3 = (instr >> 12) & 0x7;
      var rs1 = (instr >> 15) & 0x1f;
      var rs2 = (instr >> 20) & 0x1f;
      var imm12 = (instr >> 31) << 12;
      var imm11 = ((instr >> 7) & 0x1) << 11;
      var imm10_5 = ((instr >> 25) & 0x3f) << 5;
      var imm4_1 = ((instr >> 8) & 0xf) << 1;
      var imm = signExtend32(imm12 | imm11 | imm10_5 | imm4_1, 13);
      return new BInstruct(opcode, rs1, rs2, funct3, imm);
    }
  }

  public static record JInstruct(int opcode, int rd, int imm) {
    public static JInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var rd = (instr >> 7) & 0x1f;
      var imm20 = (instr >> 31) << 20;
      var imm10_1 = ((instr >> 21) & 0x3ff) << 1;
      var imm11 = ((instr >> 20) & 0x1) << 11;
      var imm19_12 = ((instr >> 12) & 0xff) << 12;
      var imm = signExtend32(imm20 | imm19_12 | imm11 | imm10_1, 21);
      return new JInstruct(opcode, rd, imm);
    }
  }
}
