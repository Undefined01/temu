package website.lihan.temu;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.nodes.Node;

import website.lihan.temu.bus.Bus;

public final class Rv64Context {
  private static final TruffleLanguage.ContextReference<Rv64Context> REF =
      TruffleLanguage.ContextReference.create(Rv64BytecodeLanguage.class);

  private final Rv64BytecodeLanguage language;

  public final Bus bus;

  public static Rv64Context get(Node node) {
    return REF.get(node);
  }

  public Rv64Context(Rv64BytecodeLanguage language, Bus bus) {
    this.language = language;
    this.bus = bus;
  }
}
