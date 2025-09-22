package website.lihan.temu.cpu;

public class Opcodes {
  public static final int LUI = 0b0110111;
  public static final int AUIPC = 0b0010111;
  public static final int JAL = 0b1101111;
  public static final int JALR = 0b1100111;

  public static final int BRANCH = 0b1100011;

  public static final int LOAD = 0b0000011;
  public static final int STORE = 0b0100011;

  public static final int OP = 0b0110011;
  public static final int OP_IMM = 0b0010011;
  public static final int OP_32 = 0b0111011;
  public static final int OP_IMM_32 = 0b0011011;

  public static final int SYSTEM = 0b1110011;
}
