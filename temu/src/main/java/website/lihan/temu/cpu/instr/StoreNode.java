package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.cpu.IllegalInstructionException;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.RvUtils.SInstruct;
import website.lihan.temu.device.Bus;
import website.lihan.temu.mm.AccessKind;
import website.lihan.temu.mm.Mapping;

@ImportStatic({MemoryAccess.class, AccessKind.class})
public abstract class StoreNode extends Node {
  public final int length;
  public final boolean isValid;
  public final int rs1;
  public final int rs2;
  public final int offset;
  public final long pc;

  protected final Bus bus;
  protected Mapping cache;

  public StoreNode(SInstruct s, long pc) {
    length =
        switch (s.funct3() & 0b11) {
          case 0b00 -> 1;
          case 0b01 -> 2;
          case 0b10 -> 4;
          case 0b11 -> 8;
          default -> 0;
        };
    isValid = length != 0;

    this.rs1 = s.rs1();
    this.rs2 = s.rs2();
    this.offset = s.imm();
    this.pc = pc;

    this.bus = Rv64Context.get(this).getBus();
  }

  public StoreNode(long pc, int length) {
    this.length = length;
    this.isValid = length != 0;
    this.rs1 = 0;
    this.rs2 = 0;
    this.offset = 0;
    this.pc = pc;

    this.bus = Rv64Context.get(this).getBus();
  }

  public void throwIfInvalid() {
    if (!isValid) {
      throw IllegalInstructionException.create(pc, "Unsupported length");
    }
  }

  public abstract void execute(Rv64State cpu, long vAddr, long value);

  @Specialization(
      guards = {"cache.inRange(vAddr)"},
      limit = "2")
  void doStore(
      Rv64State cpu,
      long vAddr,
      long value,
      @Cached("queryAddr(cpu, bus, vAddr, Store, true)") Mapping cache) {
    cache.store(pc, vAddr, length, value);
  }

  @Specialization(replaces = "doStore")
  void doLoadUncached(Rv64State cpu, long vAddr, long value) {
    if (cache == null || !(cache.vAddrStart() <= vAddr && vAddr < cache.vAddrEnd())) {
      cache = MemoryAccess.queryAddr(cpu, bus, vAddr, AccessKind.Store, false);
    }
    cache.store(pc, vAddr, length, value);
  }
}
