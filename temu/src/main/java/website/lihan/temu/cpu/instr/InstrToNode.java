package website.lihan.temu.cpu.instr;

import static website.lihan.temu.cpu.RvUtils.BYTES;

import com.oracle.truffle.api.nodes.Node;
import java.util.ArrayList;
import website.lihan.temu.cpu.Opcodes;
import website.lihan.temu.cpu.Opcodes.SystemFunct3;
import website.lihan.temu.cpu.RvUtils.BInstruct;
import website.lihan.temu.cpu.RvUtils.IInstruct;
import website.lihan.temu.cpu.RvUtils.JInstruct;
import website.lihan.temu.cpu.RvUtils.SInstruct;

public final class InstrToNode {
  public static Node[] toCachedNodes(long baseAddr, byte[] bc) {
    var nodeList = new ArrayList<Node>();
    for (int bci = 0; bci + 4 <= bc.length; bci += 4) {
      final var pc = baseAddr + bci;
      var instr = BYTES.getInt(bc, bci);
      final var opcode = instr & 0x7f;
      switch (opcode) {
        case Opcodes.JAL -> {
          final var j = JInstruct.decode(instr);
          final var nodeIdx = nodeList.size();
          nodeList.add(new JalNode(j, pc));
          instr = Opcodes.JAL | (nodeIdx << 8);
          BYTES.putInt(bc, bci, instr);
        }
        case Opcodes.JALR -> {
          final var i = IInstruct.decode(instr);
          final var returnPc = pc + 4;
          final var nodeIdx = nodeList.size();
          nodeList.add(JalrNodeGen.create(i.rs1(), i.imm(), i.rd(), returnPc));
          instr = Opcodes.JALR | (nodeIdx << 8);
          BYTES.putInt(bc, bci, instr);
        }
        case Opcodes.BRANCH -> {
          final var b = BInstruct.decode(instr);
          final var nodeIdx = nodeList.size();
          nodeList.add(new BranchNode(b, pc));
          instr = Opcodes.BRANCH | (nodeIdx << 8);
          BYTES.putInt(bc, bci, instr);
        }
        case Opcodes.LOAD -> {
          final var i = IInstruct.decode(instr);
          final var nodeIdx = nodeList.size();
          nodeList.add(LoadNodeGen.create(i, pc));
          instr = Opcodes.LOAD | (nodeIdx << 8);
          BYTES.putInt(bc, bci, instr);
        }
        case Opcodes.STORE -> {
          final var s = SInstruct.decode(instr);
          var nodeIdx = nodeList.size();
          nodeList.add(StoreNodeGen.create(s, pc));
          instr = Opcodes.STORE | (nodeIdx << 8);
          BYTES.putInt(bc, bci, instr);
        }
        case Opcodes.AMO -> {
          final var nodeIdx = nodeList.size();
          nodeList.add(new Amo(pc, instr));
          instr = Opcodes.AMO | (nodeIdx << 8);
          BYTES.putInt(bc, bci, instr);
        }
        case Opcodes.SYSTEM -> {
          final var i = IInstruct.decode(instr);
          switch (i.funct3()) {
            case SystemFunct3.CSRRW,
                SystemFunct3.CSRRS,
                SystemFunct3.CSRRC,
                SystemFunct3.CSRRWI,
                SystemFunct3.CSRRSI,
                SystemFunct3.CSRRCI -> {
              final var nodeIdx = nodeList.size();
              nodeList.add(new CsrRWNode(i, pc));
              instr = Opcodes.SYSTEM | (0x80) | (nodeIdx << 8);
              BYTES.putInt(bc, bci, instr);
            }
          }
        }
      }
    }
    var nodes = nodeList.toArray(new Node[0]);
    return nodes;
  }
}
