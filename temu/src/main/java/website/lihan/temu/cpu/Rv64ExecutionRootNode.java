package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.RootNode;
import org.graalvm.collections.EconomicMap;
import website.lihan.temu.Rv64BytecodeLanguage;
import website.lihan.temu.Utils;

public class Rv64ExecutionRootNode extends RootNode {
  Rv64BytecodeLanguage language;
  EconomicMap<Long, EconomicMap<Integer, Rv64BytecodeRootNode>> entryPoints = EconomicMap.create();

  public static final int PAGE_SIZE = 4 * 1024;
  public static final long PAGE_ADDR_MASK = 0xFFFFFFFFFFFFF000L;

  private final byte[] bc;

  public Rv64ExecutionRootNode(Rv64BytecodeLanguage language, byte[] bc) {
    super(language);
    this.language = language;
    this.bc = bc;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    long pc = 0x80000000L;
    var page = EconomicMap.<Integer, Rv64BytecodeRootNode>create();
    var pageAddr = pc & PAGE_ADDR_MASK;
    while (true) {
      try {
        // long pageAddr = pc & PAGE_ADDR_MASK;
        // var page = entryPoints.get(pageAddr);
        // if (page == null) {
        //   CompilerDirectives.transferToInterpreterAndInvalidate();
        //   page = EconomicMap.create();
        //   entryPoints.put(pageAddr, page);
        // }
        // var subAddr = (int)(pc & ~PAGE_ADDR_MASK);
        var subAddr = (int) (pc - 0x80000000L);
        var rootNode = page.get(subAddr);
        if (rootNode == null) {
          CompilerDirectives.transferToInterpreterAndInvalidate();
          var node = new Rv64BytecodeNode(pageAddr, bc, subAddr);
          rootNode = new Rv64BytecodeRootNode(language, node);
          page.put(subAddr, rootNode);
        }
        IndirectCallNode.getUncached().call(rootNode.getCallTarget());
      } catch (JumpException e) {
        pc = e.getTargetPc();
      } catch (HaltException e) {
        Utils.printf("%s\n", e);
        return 0;
      }
    }
  }
}
