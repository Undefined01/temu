package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.memory.ByteArraySupport;
import com.oracle.truffle.api.nodes.Node;
import org.graalvm.collections.EconomicMap;
import website.lihan.temu.Rv64Context;

public class ExecPageCache {
  //   public static final int PAGE_SIZE = 4 * 1024;
  // public static final long PAGE_ADDR_MASK = 0xFFFFFFFFFFFFF000L;
  public static final long PAGE_ADDR_MASK = 0xFFFFFFFFFF000000L;
  public static final int PAGE_SIZE = (int)~PAGE_ADDR_MASK + 1;

  private Rv64Context context;

  //   private final EconomicMap<Long, EconomicMap<Integer, Rv64BytecodeRootNode>> cache =
  // EconomicMap.create();
  private final EconomicMap<Integer, Rv64BytecodeRootNode> rootCache = EconomicMap.create();
  private final EconomicMap<Long, Node[]> nodeCache = EconomicMap.create();
  private final EconomicMap<Long, byte[]> bcCache = EconomicMap.create();

  public ExecPageCache(Rv64Context context) {
    this.context = context;
  }

  public Rv64BytecodeRootNode getByEntryPoint(long entryAddr) {
    var pageAddr = entryAddr & PAGE_ADDR_MASK;
    var subAddr = (int) (entryAddr - pageAddr);
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

  public Node[] getCachedNodes(long baseAddr) {
    return nodeCache.get(baseAddr);
  }

  public void putCachedNodes(long baseAddr, Node[] nodes) {
    nodeCache.put(baseAddr, nodes);
  }
}
