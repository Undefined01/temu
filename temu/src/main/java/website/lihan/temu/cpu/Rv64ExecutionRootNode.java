package website.lihan.temu.cpu;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.nodes.Node.Child;

import website.lihan.temu.Rv64BytecodeLanguage;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.csr.CsrId;
import website.lihan.temu.cpu.instr.SystemOp;
import website.lihan.temu.device.RTC;

public class Rv64ExecutionRootNode extends RootNode {
  Rv64BytecodeLanguage language;
  Rv64Context context;

  @Child
  private IndirectCallNode callNode;

  public Rv64ExecutionRootNode(Rv64BytecodeLanguage language) {
    super(language);
    this.language = language;
    this.context = Rv64Context.get(this);
    this.callNode = IndirectCallNode.create();
  }

  @Override
  public Object execute(VirtualFrame frame) {
    var cpu = this.context.getState();
    long pc = 0x80000000L;
    while (true) {
      try {
        if (cpu.isInterruptEnabled() && RTC.checkInterrupt()) {
          throw InterruptException.create(pc, InterruptException.Cause.STIMER);
        }
        // long pageAddr = pc & PAGE_ADDR_MASK;
        // var page = entryPoints.get(pageAddr);
        // if (page == null) {
        //   CompilerDirectives.transferToInterpreterAndInvalidate();
        //   page = EconomicMap.create();
        //   entryPoints.put(pageAddr, page);
        // }
        // var subAddr = (int)(pc & ~PAGE_ADDR_MASK);
        var rootNode = context.getExecPageCache().getByEntryPoint(pc, cpu);
        callNode.call(rootNode.getCallTarget(), 0);
      } catch (JumpException e) {
        // Utils.printf("JumpException to 0x%08x\n", e.getTargetPc());
        pc = e.getTargetPc();
      } catch (InterruptException e) {
        pc = SystemOp.doInterrupt(cpu, e);
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
