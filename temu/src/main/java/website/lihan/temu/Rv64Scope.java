package website.lihan.temu;

import com.oracle.truffle.api.interop.TruffleObject;
import website.lihan.temu.device.Bus;

public final class Rv64Scope implements TruffleObject {
  public Bus bus;
  public Rv64Context context;

  public Rv64Scope(Rv64Context context) {
    this.context = context;
    this.bus = context.bus;
  }
}
