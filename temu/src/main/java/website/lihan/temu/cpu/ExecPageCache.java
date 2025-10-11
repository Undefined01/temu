package website.lihan.temu.cpu;

import static website.lihan.temu.cpu.Utils.IInstruct;
import static website.lihan.temu.cpu.Utils.JInstruct;
import static website.lihan.temu.cpu.Utils.SInstruct;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.memory.ByteArraySupport;
import com.oracle.truffle.api.nodes.Node;
import java.util.ArrayList;
import org.graalvm.collections.EconomicMap;
import website.lihan.temu.Rv64Context;

public class ExecPageCache {
  public static final int PAGE_SIZE = 4 * 1024 * 1024;
  //   public static final int PAGE_SIZE = 4 * 1024;
  public static final long PAGE_ADDR_MASK = 0xFFFFFFFFFFFFF000L;

  private static final ByteArraySupport BYTES = ByteArraySupport.littleEndian();

  private Rv64Context context;

  //   private final EconomicMap<Long, EconomicMap<Integer, Rv64BytecodeRootNode>> cache =
  // EconomicMap.create();
  private final EconomicMap<Integer, Rv64BytecodeRootNode> rootCache = EconomicMap.create();
  private final EconomicMap<Long, Node[]> nodeCache = EconomicMap.create();
  private final EconomicMap<Long, byte[]> bcCache = EconomicMap.create();

  public ExecPageCache(Rv64Context context) {
    this.context = context;
  }

  public Rv64BytecodeRootNode getByEntryPoint(long pc) {
    var pageAddr = 0x80000000L;
    var subAddr = (int) (pc - 0x80000000L);
    var rootNode = rootCache.get(subAddr);
    if (rootNode == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      var bc = bcCache.get(pageAddr);
      if (bc == null) {
        bc = new byte[PAGE_SIZE];
        context.getBus().executeRead(pageAddr, bc, PAGE_SIZE);
        bcCache.put(pageAddr, bc);
      }
      var node = new Rv64BytecodeNode(pageAddr, bc, subAddr);
      rootNode = new Rv64BytecodeRootNode(context.getLanguage(), node);
      rootCache.put(subAddr, rootNode);
    }
    return rootNode;
  }

  public Node[] getCachedNodes(long baseAddr, byte[] bc) {
    if (nodeCache.containsKey(baseAddr)) {
      return nodeCache.get(baseAddr);
    }

    var nodeList = new ArrayList<Node>();
    for (int bci = 0; bci + 4 <= bc.length; bci += 4) {
      var pc = baseAddr + bci;
      var instr = BYTES.getInt(bc, bci);
      var opcode = instr & 0x7f;
      switch (opcode) {
        case Opcodes.JAL -> {
          final var j = JInstruct.decode(instr);
          var targetPc = baseAddr + bci + j.imm();
          var returnPc = baseAddr + bci + 4;
          var isCall = j.rd() == 1;
          var nodeIdx = nodeList.size();
          if (isCall) {
            // likely a function call
            nodeList.add(new CallNode(targetPc, returnPc));
          } else {
            nodeList.add(new JalNode(targetPc, returnPc, j.rd()));
          }
          instr = Opcodes.JAL | (isCall ? 0x80 : 0) | (nodeIdx << 8);
          BYTES.putInt(bc, bci, instr);
        }
        case Opcodes.LOAD -> {
          final var i = IInstruct.decode(instr);
          int length =
              switch (i.funct3() & 0b11) {
                case 0b00 -> 1;
                case 0b01 -> 2;
                case 0b10 -> 4;
                case 0b11 -> 8;
                default -> 0;
              };
          var isValidInstr = length != 0;
          var shouldSignExtend = (i.funct3() & 0b100) == 0;

          var nodeIdx = nodeList.size();
          nodeList.add(LoadNodeGen.create(length, shouldSignExtend, i.rs1(), i.rd(), i.imm()));
          instr = Opcodes.LOAD | (isValidInstr ? 0x80 : 0) | (nodeIdx << 8);
          BYTES.putInt(bc, bci, instr);
        }
        case Opcodes.STORE -> {
          final var s = SInstruct.decode(instr);
          int length =
              switch (s.funct3() & 0b11) {
                case 0b00 -> 1;
                case 0b01 -> 2;
                case 0b10 -> 4;
                case 0b11 -> 8;
                default -> 0;
              };
          var isValidInstr = length != 0;
          var nodeIdx = nodeList.size();
          nodeList.add(StoreNodeGen.create(length, s.rs1(), s.rs2(), s.imm()));
          instr = Opcodes.STORE | (isValidInstr ? 0x80 : 0) | (nodeIdx << 8);
          BYTES.putInt(bc, bci, instr);
        }
      }
    }
    var nodes = nodeList.toArray(new Node[0]);
    nodeCache.put(baseAddr, nodes);
    return nodes;
  }
}
