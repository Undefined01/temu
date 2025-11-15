package website.lihan.temu.cpu.instr;

import static website.lihan.temu.cpu.instr.Amo.Funct5.*;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.Node.Child;
import website.lihan.temu.cpu.IllegalInstructionException;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.RvUtils;

public final class Amo extends Node {
  @Child private LoadNode loadNode;
  @Child private StoreNode storeNode;

  private final long pc;
  private final int rd;
  private final int rs1;
  private final int rs2;
  private final int funct;
  private final boolean aq;
  private final boolean rl;

  public Amo(long pc, int instr) {
    final var r = RvUtils.RInstruct.decode(instr);
    this.pc = pc;
    this.rd = r.rd();
    this.rs1 = r.rs1();
    this.rs2 = r.rs2();
    var length =
        switch (r.funct3()) {
          case Funct3.W -> 4;
          case Funct3.D -> 8;
          default -> 0;
        };
    this.funct = r.funct7() >> 2;
    this.aq = (r.funct7() & 0b10) != 0;
    this.rl = (r.funct7() & 0b01) != 0;
    this.loadNode = LoadNodeGen.create(pc, length, false);
    this.storeNode = StoreNodeGen.create(pc, length);
  }

  public void execute(Rv64State cpu) {
    final var addr = cpu.getReg(rs1);
    final var value = cpu.getReg(rs2);

    switch (funct) {
      case LR -> {
        if (rs2 != 0) {
          throw IllegalInstructionException.create(pc, 0);
        }
        var res = loadNode.execute(cpu, addr);
        cpu.setReg(rd, res);
      }
      case SC -> {
        storeNode.execute(cpu, addr, value);
        cpu.setReg(rd, 0);
      }
      case AMOSWAP -> {
        var old = loadNode.execute(cpu, addr);
        storeNode.execute(cpu, addr, value);
        cpu.setReg(rd, old);
      }
      case AMOADD -> {
        var old = loadNode.execute(cpu, addr);
        var res = old + value;
        storeNode.execute(cpu, addr, res);
        cpu.setReg(rd, old);
      }
      case AMOXOR -> {
        var old = loadNode.execute(cpu, addr);
        var res = old ^ value;
        storeNode.execute(cpu, addr, res);
        cpu.setReg(rd, old);
      }
      case AMOAND -> {
        var old = loadNode.execute(cpu, addr);
        var res = old & value;
        storeNode.execute(cpu, addr, res);
        cpu.setReg(rd, old);
      }
      case AMOOR -> {
        var old = loadNode.execute(cpu, addr);
        var res = old | value;
        storeNode.execute(cpu, addr, res);
        cpu.setReg(rd, old);
      }
      case AMOMIN -> {
        var old = loadNode.execute(cpu, addr);
        var res = Long.min(old, value);
        storeNode.execute(cpu, addr, res);
        cpu.setReg(rd, old);
      }
      case AMOMAX -> {
        var old = loadNode.execute(cpu, addr);
        var res = Long.max(old, value);
        storeNode.execute(cpu, addr, res);
        cpu.setReg(rd, old);
      }
      case AMOMINU -> {
        var old = loadNode.execute(cpu, addr);
        var res = Long.compareUnsigned(old, value) < 0 ? old : value;
        storeNode.execute(cpu, addr, res);
        cpu.setReg(rd, old);
      }
      case AMOMAXU -> {
        var old = loadNode.execute(cpu, addr);
        var res = Long.compareUnsigned(old, value) > 0 ? old : value;
        storeNode.execute(cpu, addr, res);
        cpu.setReg(rd, old);
      }
      default -> throw CompilerDirectives.shouldNotReachHere();
    }
  }

  public static class Funct3 {
    public static final int W = 0b010;
    public static final int D = 0b011;
  }

  public static class Funct5 {
    public static final int LR = 0b00010;
    public static final int SC = 0b00011;
    public static final int AMOSWAP = 0b00001;
    public static final int AMOADD = 0b00000;
    public static final int AMOXOR = 0b00100;
    public static final int AMOAND = 0b01100;
    public static final int AMOOR = 0b01000;
    public static final int AMOMIN = 0b10000;
    public static final int AMOMAX = 0b10100;
    public static final int AMOMINU = 0b11000;
    public static final int AMOMAXU = 0b11100;
  }
}
