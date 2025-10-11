package website.lihan.temu.cpu.instr;

import static website.lihan.temu.cpu.RvUtils.BYTES;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.device.Bus;
import website.lihan.temu.device.DeviceLibrary;

public abstract class LoadNode extends Node {
  public final int length;
  public final boolean signedExtend;
  public final int rs1;
  public final int rd;
  public final int offset;
  private final Bus bus;

  public LoadNode(int length, boolean signedExtend, int rs1, int rd, int offset) {
    assert length == 1 || length == 2 || length == 4 || length == 8;

    this.length = length;
    this.signedExtend = signedExtend;
    this.rs1 = rs1;
    this.rd = rd;
    this.offset = offset;

    this.bus = Rv64Context.get(this).getBus();
  }

  public abstract long execute(long addr);

  @Specialization(
      guards = {"startAddress <= addr", "addr < endAddress"},
      limit = "1")
  long doLoad(
      long addr,
      @Cached("findDevice(addr)") Object device,
      @CachedLibrary("device") DeviceLibrary deviceLib,
      @Cached("deviceLib.getStartAddress(device)") long startAddress,
      @Cached("deviceLib.getEndAddress(device)") long endAddress) {
    if (signedExtend) {
      return switch (length) {
        case 1 -> deviceLib.read1(device, addr - startAddress);
        case 2 -> deviceLib.read2(device, addr - startAddress);
        case 4 -> deviceLib.read4(device, addr - startAddress);
        case 8 -> deviceLib.read8(device, addr - startAddress);
        default -> 0;
      };
    } else {
      return switch (length) {
        case 1 -> (long) deviceLib.read1(device, addr - startAddress) & 0xFFL;
        case 2 -> (long) deviceLib.read2(device, addr - startAddress) & 0xFFFFL;
        case 4 -> (long) deviceLib.read4(device, addr - startAddress) & 0xFFFFFFFFL;
        case 8 -> deviceLib.read8(device, addr - startAddress);
        default -> 0;
      };
    }
  }

  @Specialization(replaces = "doLoad")
  long doLoadUncached(long addr) {
    var data = new byte[length];
    bus.executeRead(addr, data, length);
    if (signedExtend) {
      return switch (length) {
        case 1 -> BYTES.getByte(data, 0);
        case 2 -> BYTES.getShort(data, 0);
        case 4 -> BYTES.getInt(data, 0);
        case 8 -> BYTES.getLong(data, 0);
        default -> 0;
      };
    } else {
      return switch (length) {
        case 1 -> (long) BYTES.getByte(data, 0) & 0xFFL;
        case 2 -> (long) BYTES.getShort(data, 0) & 0xFFFFL;
        case 4 -> (long) BYTES.getInt(data, 0) & 0xFFFFFFFFL;
        case 8 -> BYTES.getLong(data, 0);
        default -> 0;
      };
    }
  }

  Object findDevice(long addr) {
    return bus.findDevice(addr);
  }
}
