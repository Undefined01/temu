package website.lihan.temu;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.cpu.ExecPageCache;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.device.Bus;

public final class Rv64Context {
  private static final TruffleLanguage.ContextReference<Rv64Context> REF =
      TruffleLanguage.ContextReference.create(Rv64BytecodeLanguage.class);

  private final Rv64BytecodeLanguage language;

  public final Bus bus;
  public final Rv64State state;
  public final ExecPageCache execPageCache;

  public static Rv64Context get(Node node) {
    return REF.get(node);
  }

  public Rv64Context(Rv64BytecodeLanguage language, Bus bus) {
    this.language = language;
    this.state = new Rv64State();
    this.bus = bus;
    this.execPageCache = new ExecPageCache(this);
  }

  public Rv64BytecodeLanguage getLanguage() {
    return language;
  }

  public Rv64State getState() {
    return state;
  }

  public Bus getBus() {
    return bus;
  }

  public ExecPageCache getExecPageCache() {
    return execPageCache;
  }

  public Object createScopeObject() {
    var obj = new Rv64Scope(this);
    return obj;
  }
}
