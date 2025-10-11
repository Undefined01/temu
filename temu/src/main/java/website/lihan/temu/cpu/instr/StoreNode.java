package website.lihan.temu.cpu.instr;

import static website.lihan.temu.cpu.RvUtils.BYTES;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.device.Bus;
import website.lihan.temu.device.DeviceLibrary;

public abstract class StoreNode extends Node {
  public final int length;
  public final int rs1;
  public final int rs2;
  public final int offset;
  private final Bus bus;

  public StoreNode(int length, int rs1, int rs2, int offset) {
    assert length == 1 || length == 2 || length == 4 || length == 8;

    this.length = length;
    this.rs1 = rs1;
    this.rs2 = rs2;
    this.offset = offset;

    this.bus = Rv64Context.get(this).getBus();
  }

  public abstract void execute(long addr, long value);

  @Specialization(
      guards = {"startAddress <= addr", "addr < endAddress"},
      limit = "1")
  void doStore(
      long addr,
      long value,
      @Cached("findDevice(addr)") Object device,
      @CachedLibrary("device") DeviceLibrary deviceLib,
      @Cached("deviceLib.getStartAddress(device)") long startAddress,
      @Cached("deviceLib.getEndAddress(device)") long endAddress) {
    switch (length) {
      case 1 -> deviceLib.write1(device, addr - startAddress, (byte) value);
      case 2 -> deviceLib.write2(device, addr - startAddress, (short) value);
      case 4 -> deviceLib.write4(device, addr - startAddress, (int) value);
      case 8 -> deviceLib.write8(device, addr - startAddress, value);
    }
  }

  @Specialization(replaces = "doStore")
  @ExplodeLoop
  void doStoreUncached(long addr, long value) {
    var data = new byte[length];
    switch (length) {
      case 1 -> BYTES.putByte(data, 0, (byte) value);
      case 2 -> BYTES.putShort(data, 0, (short) value);
      case 4 -> BYTES.putInt(data, 0, (int) value);
      case 8 -> BYTES.putLong(data, 0, value);
    }
    bus.executeWrite(addr, data, length);
  }

  Object findDevice(long addr) {
    return bus.findDevice(addr);
  }
}
