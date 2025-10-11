package website.lihan.temu.cpu;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.RootNode;
import website.lihan.temu.Rv64BytecodeLanguage;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.Utils;

public class Rv64ExecutionRootNode extends RootNode {
  Rv64BytecodeLanguage language;
  Rv64Context context;

  private final byte[] bc;

  public Rv64ExecutionRootNode(Rv64BytecodeLanguage language, byte[] bc) {
    super(language);
    this.language = language;
    this.context = Rv64Context.get(this);
    this.bc = bc;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    var cpu = this.context.getState();
    var callNode = IndirectCallNode.create();
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
        var rootNode = context.getExecPageCache().getByEntryPoint(cpu.pc);
        callNode.call(rootNode.getCallTarget(), cpu);
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
        Utils.printStackTrace(t);
        return -1;
      }
    }
  }
}
