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
  public static final int MEM_MISC = 0b0001111;

  public static final int AMO = 0b0101111;

  public static final int SYSTEM = 0b1110011;

  public class SystemFunct3 {
    public static final int PRIV = 0b000;
    public static final int CSRRW = 0b001;
    public static final int CSRRS = 0b010;
    public static final int CSRRC = 0b011;
    public static final int PRIVM = 0b100;
    public static final int CSRRWI = 0b101;
    public static final int CSRRSI = 0b110;
    public static final int CSRRCI = 0b111;
  }

  public class SystemFunct7 {
    public static final int SFENCE_VMA = 0b0001001;
  }

  public class SystemFunct12 {
    public static final int ECALL = 0b000000000000;
    public static final int EBREAK = 0b000000000001;
    public static final int SRET = 0b000100000010;
    public static final int MRET = 0b001100000010;
    public static final int MNRET = 0b011100000010;
  }
}
