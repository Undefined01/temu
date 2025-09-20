package website.lihan.temu.cpu;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import website.lihan.temu.Rv64BytecodeLanguage;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.Utils;

public class Rv64BytecodeRootNode extends RootNode {
  @Child Rv64BytecodeNode bytecodeNode;

  public Rv64BytecodeRootNode(Rv64BytecodeLanguage language, Rv64BytecodeNode bytecodeNode) {
    super(language);
    this.bytecodeNode = bytecodeNode;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    long pc = 0x80000000L;
    while (true) {
      try {
        bytecodeNode.execute(frame);
      } catch (IllegalInstructionException e) {
        var context = Rv64Context.get(this);
        pc = context.getState().pc;
        Utils.printf("%s at PC = %08x\n", e, pc);
        return 0;
      } catch (JumpException e) {
        pc = e.getTargetPc();
      } catch (HaltException e) {
        Utils.printf("%s\n", e);
        return 0;
      }
    }
  }
}
