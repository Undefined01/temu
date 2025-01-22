package website.lihan.temu.cpu;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;

public class Riscv64BytecodeRootNode extends RootNode {
    @Child
    Riscv64BytecodeNode cpu;
    
    public Riscv64BytecodeRootNode(Riscv64BytecodeNode cpu) {
        super(null);
        this.cpu = cpu;
    }
    
    @Override
    public Object execute(VirtualFrame frame) {
        return cpu.execute(frame);
    }
}
