package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.nodes.Node;
import org.graalvm.collections.EconomicMap;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.instr.InstrToNode;
import website.lihan.temu.cpu.instr.MemoryAccess;
import website.lihan.temu.device.Bus;
import website.lihan.temu.mm.AccessKind;
import website.lihan.temu.mm.MemoryException;

public class ExecPageCache {
  @CompilationFinal public static int MAX_PAGE_SIZE = 1024 * 1024; // 1MB

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

  public Rv64BytecodeRootNode getByEntryPoint(Rv64State cpu, long vEntryAddr) {
    var result = MemoryAccess.queryAddr(cpu, bus, vEntryAddr, AccessKind.Execute, false);
    if (result.exception() != MemoryException.None) {
      throw InterruptException.create(
          vEntryAddr, result.exception().toExecuteException(), vEntryAddr);
    }
    var pAddr = result.toPAddr(vEntryAddr);
    var pAddrStart = result.pAddrStart();
    var vAddrStart = result.vAddrStart();
    var pageSize = (int) (result.vAddrEnd() - result.vAddrStart());

    var subAddr = (int) (pAddr - pAddrStart);
    var rootNode = rootCache.get(vEntryAddr);
    if (rootNode == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      var bc = bcCache.get(vAddrStart);
      var cachedNodes = nodeCache.get(vAddrStart);
      if (bc == null) {
        bc = new byte[pageSize];
        context.getBus().executeRead(pAddrStart, bc, pageSize);
        bcCache.put(vAddrStart, bc);
        cachedNodes = InstrToNode.toCachedNodes(vAddrStart, bc);
        nodeCache.put(vAddrStart, cachedNodes);
      }
      if (vEntryAddr >= vAddrStart + bc.length) {
        Utils.printf("vaddr=%x, [%x, %x)\n", vEntryAddr, vAddrStart, vAddrStart + bc.length);
        throw CompilerDirectives.shouldNotReachHere();
      }
      var node = new Rv64BytecodeNode(vAddrStart, bc, cachedNodes, subAddr, cpu);
      rootNode = new Rv64BytecodeRootNode(context.getLanguage(), node);
      rootCache.put(pAddr, rootNode);
    }
    return rootNode;
  }

  public void clear() {
    rootCache.clear();
    nodeCache.clear();
    bcCache.clear();
  }
}
