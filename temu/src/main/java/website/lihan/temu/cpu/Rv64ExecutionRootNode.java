package website.lihan.temu.cpu;

import org.graalvm.collections.EconomicMap;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.RootNode;
import org.graalvm.collections.EconomicMap;
import website.lihan.temu.Rv64BytecodeLanguage;
import website.lihan.temu.bus.Bus;
import website.lihan.temu.Rv64Context;

public class Rv64ExecutionRootNode extends RootNode {
  Rv64BytecodeLanguage language;
  @Child Bus bus;
  EconomicMap<Long, EconomicMap<Integer, Rv64BytecodeRootNode>> entryPoints = EconomicMap.create();

  public static final int PAGE_SIZE = 4 * 1024;
  public static final long PAGE_ADDR_MASK = 0xFFFFFFFFFFFFF000L;

  public Rv64ExecutionRootNode(Rv64BytecodeLanguage language, Bus bus) {
    super(language);
    this.language = language;
    this.bus = bus;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    long pc = 0x80000000L;
    while (true) {
      try {
        long pageAddr = pc & PAGE_ADDR_MASK;
        var page = entryPoints.get(pageAddr);
        if (page == null) {
          CompilerDirectives.transferToInterpreterAndInvalidate();
          page = EconomicMap.create();
          entryPoints.put(pageAddr, page);
        }
        var subAddr = (int)(pc & ~PAGE_ADDR_MASK);
        var rootNode = page.get(subAddr);
        if (rootNode == null) {
          CompilerDirectives.transferToInterpreterAndInvalidate();
          var bc = new byte[PAGE_SIZE];
          bus.executeRead(pageAddr, bc, bc.length);
          var node = new Rv64BytecodeNode(bus, pageAddr, bc, subAddr);
          rootNode = new Rv64BytecodeRootNode(language, node);
          page.put(subAddr, rootNode);
        }
        return IndirectCallNode.getUncached().call(rootNode.getCallTarget());
      } catch (IllegalInstructionException e) {
        print(e);
        break;
      } catch (JumpException e) {
        pc = e.getTargetPc();
      } catch (HaltException e) {
        print(e);
        break;
      }
    }
    return 0;
  }

  @TruffleBoundary
  private void print(Exception e) {
    System.out.println(e.getMessage());
  }
}
