package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.cpu.IllegalInstructionException;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.RvUtils.IInstruct;
import website.lihan.temu.device.Bus;
import website.lihan.temu.mm.AccessKind;
import website.lihan.temu.mm.Mapping;

@ImportStatic({MemoryAccess.class, AccessKind.class})
public abstract class LoadNode extends Node {
  public final int length;
  public final boolean isValid;
  public final boolean signedExtend;
  public final int rd;
  public final int rs1;
  public final int offset;
  public final long pc;

  protected final Bus bus;
  protected Mapping cache;

  public LoadNode(IInstruct i, long pc) {
    length =
        switch (i.funct3() & 0b11) {
          case 0b00 -> 1;
          case 0b01 -> 2;
          case 0b10 -> 4;
          case 0b11 -> 8;
          default -> 0;
        };
    isValid = length != 0;
    signedExtend = (i.funct3() & 0b100) == 0;

    this.rs1 = i.rs1();
    this.rd = i.rd();
    this.offset = i.imm();
    this.pc = pc;

    this.bus = Rv64Context.get(this).getBus();
  }

  public LoadNode(long pc, int length, boolean signedExtend) {
    this.length = length;
    this.isValid = true;
    this.signedExtend = signedExtend;
    this.rd = 0;
    this.rs1 = 0;
    this.offset = 0;
    this.pc = pc;
    this.bus = Rv64Context.get(this).getBus();
  }

  public void throwIfInvalid() {
    if (!isValid) {
      throw IllegalInstructionException.create(pc, 0);
    }
  }

  public abstract long execute(Rv64State cpu, long vAddr);

  @Specialization(
      guards = {"cache.inRange(vAddr)"},
      limit = "2")
  long doLoad(
      Rv64State cpu, long vAddr, @Cached("queryAddr(cpu, bus, vAddr, Load, true)") Mapping cache) {
    return cache.load(pc, vAddr, length, signedExtend);
  }

  @Specialization(replaces = "doLoad")
  long doLoadUncached(Rv64State cpu, long vAddr) {
    if (cache == null || !(cache.vAddrStart() <= vAddr && vAddr < cache.vAddrEnd())) {
      cache = MemoryAccess.queryAddr(cpu, bus, vAddr, AccessKind.Load, false);
    }
    return cache.load(pc, vAddr, length, signedExtend);
  }
}
