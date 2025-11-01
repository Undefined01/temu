package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import org.graalvm.collections.EconomicMap;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.cpu.instr.MemoryAccess;
import website.lihan.temu.cpu.instr.MemoryAccess.AccessKind;
import website.lihan.temu.device.Bus;

public class ExecPageCache {
  public static final long PAGE_ADDR_MASK = 0xFFFFFFFFFFFFF000L;
  // public static final long PAGE_ADDR_MASK = 0xFFFFFFFFFF000000L;
  public static final int PAGE_SIZE = (int) ~PAGE_ADDR_MASK + 1;

  private final Rv64Context context;
  private final Bus bus;

  //   private final EconomicMap<Long, EconomicMap<Integer, Rv64BytecodeRootNode>> cache =
  // EconomicMap.create();
  private final EconomicMap<Long, Rv64BytecodeRootNode> rootCache = EconomicMap.create();
  private final EconomicMap<Long, Node[]> nodeCache = EconomicMap.create();
  private final EconomicMap<Long, byte[]> bcCache = EconomicMap.create();

  public ExecPageCache(Rv64Context context) {
    this.context = context;
    this.bus = context.getBus();
  }

  @TruffleBoundary
  public Rv64BytecodeRootNode getByEntryPoint(Rv64State cpu, long entryPc) {
    var satp = cpu.getCsrFile().satp.getValue();
    long pAddr;
    if (satp != 0) {
      var result = MemoryAccess.queryAddr(cpu, bus, entryPc, AccessKind.Execute, false);
      pAddr = result.toPAddr(entryPc);
    } else {
      pAddr = entryPc;
    }
    var pageAddr = pAddr & PAGE_ADDR_MASK;
    var subAddr = (int) (pAddr - pageAddr);
    var rootNode = rootCache.get(pAddr);
    if (rootNode == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      var bc = bcCache.get(pageAddr);
      if (bc == null) {
        bc = new byte[PAGE_SIZE];
        context.getBus().executeRead(pageAddr, bc, PAGE_SIZE);
        bcCache.put(pageAddr, bc);
      }
      var node = new Rv64BytecodeNode(pageAddr, bc, subAddr, cpu);
      rootNode = new Rv64BytecodeRootNode(context.getLanguage(), node);
      rootCache.put(pAddr, rootNode);
    }
    return rootNode;
  }

  public Node[] getCachedNodes(long baseAddr) {
    return nodeCache.get(baseAddr);
  }

  public void putCachedNodes(long baseAddr, Node[] nodes) {
    nodeCache.put(baseAddr, nodes);
  }
}
