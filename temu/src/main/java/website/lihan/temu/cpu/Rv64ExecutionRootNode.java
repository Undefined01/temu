package website.lihan.temu.cpu;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.RootNode;
import website.lihan.temu.Rv64BytecodeLanguage;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.instr.SystemOp;

public class Rv64ExecutionRootNode extends RootNode {
  Rv64BytecodeLanguage language;
  Rv64Context context;

  @Child private IndirectCallNode callNode;

  public Rv64ExecutionRootNode(Rv64BytecodeLanguage language) {
    super(language);
    this.language = language;
    this.context = Rv64Context.get(this);
    this.callNode = IndirectCallNode.create();
  }

  @Override
  public Object execute(VirtualFrame frame) {
    var cpu = this.context.getState();
    var pageCache = this.context.getExecPageCache();
    long pc = 0x80000000L;
    while (true) {
      try {
        cpu.throwPendingInterrupt(pc);
        var rootNode = pageCache.getByEntryPoint(cpu, pc);
        callNode.call(rootNode.getCallTarget(), 0);
      } catch (JumpException e) {
        // Utils.printf("JumpException to 0x%08x\n", e.getTargetPc());
        pc = e.getTargetPc();
      } catch (InterruptException e) {
        pc = SystemOp.doInterrupt(cpu, e);
        // Utils.printf("InterruptException from 0x%08x to 0x%08x, cause=%d\n", e.pc, pc, e.cause);
      } catch (HaltException e) {
        Utils.printf("%s\n", e);
        return 0;
      } catch (IllegalInstructionException t) {
        Utils.printf("Unexpected exception: %s\n", t);
        Utils.printStackTrace(t);
        return -1;
      } catch (Throwable t) {
        Utils.printf("Unexpected exception: %s\n", t);
        Utils.printStackTrace(t);
        return -1;
      }
    }
  }
}
