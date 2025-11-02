package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import org.graalvm.collections.EconomicMap;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.cpu.instr.MemoryAccess;
import website.lihan.temu.cpu.instr.MemoryAccess.AccessKind;
import website.lihan.temu.cpu.instr.MemoryAccess.MemoryException;
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
  public Rv64BytecodeRootNode getByEntryPoint(Rv64State cpu, long vEntryAddr) {
    var satp = cpu.getCsrFile().satp.getValue();
    long pAddr;
    if (satp != 0) {
      var result = MemoryAccess.queryAddr(cpu, bus, vEntryAddr, AccessKind.Execute, false);
      if (result.exception() != MemoryException.None) {
        throw InterruptException.create(vEntryAddr, result.exception().toExecuteException(), vEntryAddr);
      }
      pAddr = result.toPAddr(vEntryAddr);
    } else {
      pAddr = vEntryAddr;
    }
    var vPageAddr = vEntryAddr & PAGE_ADDR_MASK;
    var pPageAddr = pAddr & PAGE_ADDR_MASK;
    var subAddr = (int) (pAddr & ~PAGE_ADDR_MASK);
    var rootNode = rootCache.get(vEntryAddr);
    if (rootNode == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      var bc = bcCache.get(vPageAddr);
      if (bc == null) {
        bc = new byte[PAGE_SIZE];
        context.getBus().executeRead(pPageAddr, bc, PAGE_SIZE);
        bcCache.put(vPageAddr, bc);
      }
      var node = new Rv64BytecodeNode(vPageAddr, bc, subAddr, cpu);
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

  public void clear() {
    rootCache.clear();
    nodeCache.clear();
    bcCache.clear();
  }
}
