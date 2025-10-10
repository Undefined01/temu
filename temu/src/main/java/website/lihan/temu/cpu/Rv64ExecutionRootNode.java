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
    var cpu = new Rv64State();
    var page = EconomicMap.<Integer, Rv64BytecodeRootNode>create();
    var pageAddr = cpu.pc & PAGE_ADDR_MASK;
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
        var subAddr = (int) (cpu.pc - 0x80000000L);
        var rootNode = page.get(subAddr);
        if (rootNode == null) {
          CompilerDirectives.transferToInterpreterAndInvalidate();
          var node = new Rv64BytecodeNode(pageAddr, bc, subAddr);
          rootNode = new Rv64BytecodeRootNode(language, node);
          page.put(subAddr, rootNode);
        }
        IndirectCallNode.getUncached().call(rootNode.getCallTarget(), cpu);
      } catch (JumpException e) {
        cpu.pc = e.getTargetPc();
      } catch (InterruptException e) {
        cpu.writeCSR(Rv64State.CSR.SEPC, e.pc);
        cpu.writeCSR(Rv64State.CSR.SCAUSE, e.cause);
        var mtvec = cpu.readCSR(Rv64State.CSR.STVEC);
        // Utils.printf("Interrupt: pc=%08x, cause=%d, jumps to vec=%08x\n", e.pc, e.cause, mtvec);
        cpu.pc = mtvec;
      } catch (HaltException e) {
        Utils.printf("%s\n", e);
        return 0;
      } catch (Throwable t) {
        Utils.printf("Unexpected exception: %s\n", t);
        t.printStackTrace();
        return -1;
      }
    }
  }
}
