package website.lihan.temu.cpu;

public class Opcodes {
    public static final int LUI = 0b0110111;
    public static final int AUIPC = 0b0010111;
    public static final int JAL = 0b1101111;
    public static final int JALR = 0b1100111;

    public static final int BRANCH = 0b1100011;

    public static final int LOAD = 0b0000011;
    public static final int STORE = 0b0100011;

    public static final int ARITHMETIC = 0b0110011;
    public static final int ARITHMETIC_IMM = 0b0010011;
    public static final int ARITHMETIC32 = 0b0111011;
    public static final int ARITHMETIC32_IMM = 0b0011011;

    // custom opcodes for the emulator
    public static final int HALT = 0b01101011;
}
