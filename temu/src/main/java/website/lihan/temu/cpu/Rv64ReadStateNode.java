package website.lihan.temu.cpu;

import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;

public final class Rv64ReadStateNode extends Node {
  public Rv64State execute() {
    return Rv64Context.get(this).getState();
  }
}
