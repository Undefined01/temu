// CheckStyle: start generated
package website.lihan.temu;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.HostCompilerDirectives;
import com.oracle.truffle.api.HostCompilerDirectives.BytecodeInterpreterSwitch;
import com.oracle.truffle.api.HostCompilerDirectives.InliningCutoff;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.TruffleStackTraceElement;
import com.oracle.truffle.api.bytecode.BytecodeBuilder;
import com.oracle.truffle.api.bytecode.BytecodeConfig;
import com.oracle.truffle.api.bytecode.BytecodeConfigEncoder;
import com.oracle.truffle.api.bytecode.BytecodeDSLAccess;
import com.oracle.truffle.api.bytecode.BytecodeEncodingException;
import com.oracle.truffle.api.bytecode.BytecodeLabel;
import com.oracle.truffle.api.bytecode.BytecodeLocal;
import com.oracle.truffle.api.bytecode.BytecodeLocation;
import com.oracle.truffle.api.bytecode.BytecodeNode;
import com.oracle.truffle.api.bytecode.BytecodeParser;
import com.oracle.truffle.api.bytecode.BytecodeRootNodes;
import com.oracle.truffle.api.bytecode.BytecodeSupport.CloneReferenceList;
import com.oracle.truffle.api.bytecode.BytecodeSupport.ConstantsBuffer;
import com.oracle.truffle.api.bytecode.BytecodeTier;
import com.oracle.truffle.api.bytecode.ExceptionHandler;
import com.oracle.truffle.api.bytecode.Instruction;
import com.oracle.truffle.api.bytecode.Instruction.Argument;
import com.oracle.truffle.api.bytecode.LocalVariable;
import com.oracle.truffle.api.bytecode.SourceInformation;
import com.oracle.truffle.api.bytecode.SourceInformationTree;
import com.oracle.truffle.api.bytecode.TagTree;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameExtensions;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags.ExpressionTag;
import com.oracle.truffle.api.instrumentation.StandardTags.RootBodyTag;
import com.oracle.truffle.api.instrumentation.StandardTags.RootTag;
import com.oracle.truffle.api.instrumentation.StandardTags.StatementTag;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.memory.ByteArraySupport;
import com.oracle.truffle.api.nodes.BytecodeOSRNode;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.ExplodeLoop.LoopExplosionKind;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import java.lang.invoke.VarHandle;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/*
 * operations:
 *   - Operation Block
 *     kind: BLOCK
 *   - Operation Root
 *     kind: ROOT
 *   - Operation IfThen
 *     kind: IF_THEN
 *   - Operation IfThenElse
 *     kind: IF_THEN_ELSE
 *   - Operation Conditional
 *     kind: CONDITIONAL
 *   - Operation While
 *     kind: WHILE
 *   - Operation TryCatch
 *     kind: TRY_CATCH
 *   - Operation TryFinally
 *     kind: TRY_FINALLY
 *   - Operation TryCatchOtherwise
 *     kind: TRY_CATCH_OTHERWISE
 *   - Operation FinallyHandler
 *     kind: FINALLY_HANDLER
 *   - Operation Label
 *     kind: LABEL
 *   - Operation Branch
 *     kind: BRANCH
 *   - Operation LoadConstant
 *     kind: LOAD_CONSTANT
 *   - Operation LoadNull
 *     kind: LOAD_NULL
 *   - Operation LoadArgument
 *     kind: LOAD_ARGUMENT
 *   - Operation LoadException
 *     kind: LOAD_EXCEPTION
 *   - Operation LoadLocal
 *     kind: LOAD_LOCAL
 *   - Operation StoreLocal
 *     kind: STORE_LOCAL
 *   - Operation Return
 *     kind: RETURN
 *   - Operation Source
 *     kind: SOURCE
 *   - Operation SourceSectionPrefix
 *     kind: SOURCE_SECTION
 *   - Operation SourceSectionSuffix
 *     kind: SOURCE_SECTION
 *   - Operation Add
 *     kind: CUSTOM
 *   - Operation Div
 *     kind: CUSTOM
 *   - Operation Equals
 *     kind: CUSTOM
 *   - Operation LessThan
 *     kind: CUSTOM
 *   - Operation EagerOr
 *     kind: CUSTOM
 *   - Operation ToBool
 *     kind: CUSTOM
 *   - Operation ArrayLength
 *     kind: CUSTOM
 *   - Operation ArrayIndex
 *     kind: CUSTOM
 *   - Operation ScOr
 *     kind: CUSTOM_SHORT_CIRCUIT
 * instructions:
 *   - Instruction load.argument
 *     kind: LOAD_ARGUMENT
 *     encoding: [1 : short, index (short) : short]
 *     signature: Object ()
 *   - Instruction load.constant
 *     kind: LOAD_CONSTANT
 *     encoding: [2 : short, constant (const) : int]
 *     signature: Object ()
 *   - Instruction load.local
 *     kind: LOAD_LOCAL
 *     encoding: [3 : short, frame_index : short]
 *     signature: Object ()
 *   - Instruction clear.local
 *     kind: CLEAR_LOCAL
 *     encoding: [4 : short, frame_index : short]
 *     signature: void ()
 *   - Instruction store.local
 *     kind: STORE_LOCAL
 *     encoding: [5 : short, frame_index : short]
 *     signature: void (Object)
 *   - Instruction branch
 *     kind: BRANCH
 *     encoding: [6 : short, branch_target (bci) : int]
 *     signature: void ()
 *   - Instruction branch.backward
 *     kind: BRANCH_BACKWARD
 *     encoding: [7 : short, branch_target (bci) : int, loop_header_branch_profile (branch_profile) : int]
 *     signature: void ()
 *   - Instruction branch.false
 *     kind: BRANCH_FALSE
 *     encoding: [8 : short, branch_target (bci) : int, branch_profile : int]
 *     signature: void (Object)
 *   - Instruction pop
 *     kind: POP
 *     encoding: [9 : short]
 *     signature: void (Object)
 *   - Instruction dup
 *     kind: DUP
 *     encoding: [10 : short]
 *     signature: void ()
 *   - Instruction load.null
 *     kind: LOAD_NULL
 *     encoding: [11 : short]
 *     signature: Object ()
 *   - Instruction return
 *     kind: RETURN
 *     encoding: [12 : short]
 *     signature: void (Object)
 *   - Instruction throw
 *     kind: THROW
 *     encoding: [13 : short]
 *     signature: void (Object)
 *   - Instruction load.exception
 *     kind: LOAD_EXCEPTION
 *     encoding: [14 : short, exception_sp (sp) : short]
 *     signature: Object ()
 *   - Instruction c.Add
 *     kind: CUSTOM
 *     encoding: [15 : short, node : int]
 *     nodeType: Add
 *     signature: int (int, int)
 *   - Instruction c.Div
 *     kind: CUSTOM
 *     encoding: [16 : short, node : int]
 *     nodeType: Div
 *     signature: int (int, int)
 *   - Instruction c.Equals
 *     kind: CUSTOM
 *     encoding: [17 : short, node : int]
 *     nodeType: Equals
 *     signature: boolean (int, int)
 *   - Instruction c.LessThan
 *     kind: CUSTOM
 *     encoding: [18 : short, node : int]
 *     nodeType: LessThan
 *     signature: boolean (int, int)
 *   - Instruction c.EagerOr
 *     kind: CUSTOM
 *     encoding: [19 : short, node : int]
 *     nodeType: EagerOr
 *     signature: boolean (boolean, boolean)
 *   - Instruction c.ToBool
 *     kind: CUSTOM
 *     encoding: [20 : short, node : int]
 *     nodeType: ToBool
 *     signature: boolean (Object)
 *   - Instruction c.ArrayLength
 *     kind: CUSTOM
 *     encoding: [21 : short, node : int]
 *     nodeType: ArrayLength
 *     signature: int (int[])
 *   - Instruction c.ArrayIndex
 *     kind: CUSTOM
 *     encoding: [22 : short, node : int]
 *     nodeType: ArrayIndex
 *     signature: int (int[], int)
 *   - Instruction sc.ScOr
 *     kind: CUSTOM_SHORT_CIRCUIT
 *     encoding: [23 : short, branch_target (bci) : int, branch_profile : int]
 *     signature: boolean (boolean, boolean)
 *   - Instruction invalidate0
 *     kind: INVALIDATE
 *     encoding: [24 : short]
 *     signature: void ()
 *   - Instruction invalidate1
 *     kind: INVALIDATE
 *     encoding: [25 : short, invalidated0 (short) : short]
 *     signature: void ()
 *   - Instruction invalidate2
 *     kind: INVALIDATE
 *     encoding: [26 : short, invalidated0 (short) : short, invalidated1 (short) : short]
 *     signature: void ()
 *   - Instruction invalidate3
 *     kind: INVALIDATE
 *     encoding: [27 : short, invalidated0 (short) : short, invalidated1 (short) : short, invalidated2 (short) : short]
 *     signature: void ()
 *   - Instruction invalidate4
 *     kind: INVALIDATE
 *     encoding: [28 : short, invalidated0 (short) : short, invalidated1 (short) : short, invalidated2 (short) : short, invalidated3 (short) : short]
 *     signature: void ()
 */
@SuppressWarnings({"javadoc", "unused", "deprecation", "static-method"})
public final class GettingStartedBytecodeRootNodeGen extends GettingStartedBytecodeRootNode {
  private static final int[] EMPTY_INT_ARRAY = new int[0];
  private static final Object[] EMPTY_ARRAY = new Object[0];
  private static final BytecodeDSLAccess ACCESS =
      BytecodeDSLAccess.lookup(BytecodeRootNodesImpl.VISIBLE_TOKEN, true);
  private static final ByteArraySupport BYTES = ACCESS.getByteArraySupport();
  private static final FrameExtensions FRAMES = ACCESS.getFrameExtensions();
  private static final int USER_LOCALS_START_INDEX = 0;
  private static final AtomicReferenceFieldUpdater<
          GettingStartedBytecodeRootNodeGen, AbstractBytecodeNode>
      BYTECODE_UPDATER =
          AtomicReferenceFieldUpdater.newUpdater(
              GettingStartedBytecodeRootNodeGen.class, AbstractBytecodeNode.class, "bytecode");
  private static final Class<? extends VirtualFrame> FRAME_TYPE =
      Truffle.getRuntime()
          .createVirtualFrame(EMPTY_ARRAY, FrameDescriptor.newBuilder().build())
          .getClass();
  private static final int LOCALS_OFFSET_START_BCI = 0;
  private static final int LOCALS_OFFSET_END_BCI = 1;
  private static final int LOCALS_OFFSET_LOCAL_INDEX = 2;
  private static final int LOCALS_OFFSET_FRAME_INDEX = 3;
  private static final int LOCALS_OFFSET_NAME = 4;
  private static final int LOCALS_OFFSET_INFO = 5;
  private static final int LOCALS_LENGTH = 6;
  private static final int EXCEPTION_HANDLER_OFFSET_START_BCI = 0;
  private static final int EXCEPTION_HANDLER_OFFSET_END_BCI = 1;
  private static final int EXCEPTION_HANDLER_OFFSET_KIND = 2;
  private static final int EXCEPTION_HANDLER_OFFSET_HANDLER_BCI = 3;
  private static final int EXCEPTION_HANDLER_OFFSET_HANDLER_SP = 4;
  private static final int EXCEPTION_HANDLER_LENGTH = 5;
  private static final int SOURCE_INFO_OFFSET_START_BCI = 0;
  private static final int SOURCE_INFO_OFFSET_END_BCI = 1;
  private static final int SOURCE_INFO_OFFSET_SOURCE = 2;
  private static final int SOURCE_INFO_OFFSET_START = 3;
  private static final int SOURCE_INFO_OFFSET_LENGTH = 4;
  private static final int SOURCE_INFO_LENGTH = 5;
  private static final int HANDLER_CUSTOM = 0;
  private static final int VARIADIC_STACK_LIMIT = (32);
  private static final ConcurrentHashMap<Integer, Class<? extends Tag>[]> TAG_MASK_TO_TAGS =
      new ConcurrentHashMap<>();
  private static final ClassValue<Integer> CLASS_TO_TAG_MASK =
      GettingStartedBytecodeRootNodeGen.initializeTagMaskToClass();

  @Child private volatile AbstractBytecodeNode bytecode;
  private final BytecodeRootNodesImpl nodes;

  /** The number of frame slots required for locals. */
  private final int maxLocals;

  private final int buildIndex;
  private CloneReferenceList<GettingStartedBytecodeRootNodeGen> clones;

  private GettingStartedBytecodeRootNodeGen(
      BytecodeDSLTestLanguage language,
      com.oracle.truffle.api.frame.FrameDescriptor.Builder builder,
      BytecodeRootNodesImpl nodes,
      int maxLocals,
      int buildIndex,
      byte[] bytecodes,
      Object[] constants,
      int[] handlers,
      int[] locals,
      int[] sourceInfo,
      List<Source> sources,
      int numNodes) {
    super(language, builder.build());
    this.nodes = nodes;
    this.maxLocals = maxLocals;
    this.buildIndex = buildIndex;
    this.bytecode =
        insert(
            new UninitializedBytecodeNode(
                bytecodes, constants, handlers, locals, sourceInfo, sources, numNodes));
  }

  @Override
  public Object execute(VirtualFrame frame) {
    return continueAt(bytecode, 0, maxLocals, frame);
  }

  @SuppressWarnings("all")
  private Object continueAt(AbstractBytecodeNode bc, int bci, int sp, VirtualFrame frame) {
    long state = ((sp & 0xFFFFL) << 32) | (bci & 0xFFFFFFFFL);
    while (true) {
      state = bc.continueAt(this, frame, state);
      if ((int) state == 0xFFFFFFFF) {
        break;
      } else {
        // Bytecode or tier changed
        CompilerDirectives.transferToInterpreterAndInvalidate();
        AbstractBytecodeNode oldBytecode = bc;
        bc = this.bytecode;
        state = oldBytecode.transitionState(bc, state);
      }
    }
    return FRAMES.uncheckedGetObject(frame, (short) (state >>> 32));
  }

  private void transitionToCached() {
    CompilerDirectives.transferToInterpreterAndInvalidate();
    AbstractBytecodeNode oldBytecode;
    AbstractBytecodeNode newBytecode;
    do {
      oldBytecode = this.bytecode;
      newBytecode = insert(oldBytecode.toCached());
      VarHandle.storeStoreFence();
      if (oldBytecode == newBytecode) {
        return;
      }
    } while (!BYTECODE_UPDATER.compareAndSet(this, oldBytecode, newBytecode));
  }

  private AbstractBytecodeNode updateBytecode(
      byte[] bytecodes_,
      Object[] constants_,
      int[] handlers_,
      int[] locals_,
      int[] sourceInfo_,
      List<Source> sources_,
      int numNodes_,
      CharSequence reason) {
    CompilerAsserts.neverPartOfCompilation();
    AbstractBytecodeNode oldBytecode;
    AbstractBytecodeNode newBytecode;
    do {
      oldBytecode = this.bytecode;
      newBytecode =
          insert(
              oldBytecode.update(
                  bytecodes_, constants_, handlers_, locals_, sourceInfo_, sources_, numNodes_));
      if (bytecodes_ == null) {
        // When bytecode doesn't change, nodes are reused and should be re-adopted.
        newBytecode.adoptNodesAfterUpdate();
      }
      VarHandle.storeStoreFence();
    } while (!BYTECODE_UPDATER.compareAndSet(this, oldBytecode, newBytecode));

    if (bytecodes_ != null) {
      oldBytecode.invalidate(newBytecode, reason);
    }
    assert Thread.holdsLock(this.nodes);
    var cloneReferences = this.clones;
    if (cloneReferences != null) {
      cloneReferences.forEach(
          (clone) -> {
            clone.updateBytecode(
                bytecodes_ != null ? unquickenBytecode(bytecodes_) : null,
                constants_,
                handlers_,
                locals_,
                sourceInfo_,
                sources_,
                numNodes_,
                reason);
          });
    }
    return newBytecode;
  }

  @Override
  protected boolean isInstrumentable() {
    return false;
  }

  @Override
  @TruffleBoundary
  protected void prepareForCall() {
    if (!this.nodes.isParsed()) {
      throw new IllegalStateException(
          "A call target cannot be created until bytecode parsing completes. Request a call target after the parse is complete instead.");
    }
  }

  @Override
  public boolean isCloningAllowed() {
    return true;
  }

  @Override
  protected boolean isCloneUninitializedSupported() {
    return true;
  }

  @Override
  protected RootNode cloneUninitialized() {
    GettingStartedBytecodeRootNodeGen clone;
    synchronized (nodes) {
      clone = (GettingStartedBytecodeRootNodeGen) this.copy();
      clone.clones = null;
      clone.bytecode = insert(this.bytecode.cloneUninitialized());
      CloneReferenceList<GettingStartedBytecodeRootNodeGen> localClones = this.clones;
      if (localClones == null) {
        this.clones = localClones = new CloneReferenceList<GettingStartedBytecodeRootNodeGen>();
      }
      localClones.add(clone);
    }
    VarHandle.storeStoreFence();
    return clone;
  }

  @Override
  @SuppressWarnings("hiding")
  protected int findBytecodeIndex(Node node, Frame frame) {
    AbstractBytecodeNode bytecode = null;
    Node prev = node;
    Node current = node;
    while (current != null) {
      if (current instanceof AbstractBytecodeNode b) {
        bytecode = b;
        break;
      }
      prev = current;
      current = prev.getParent();
    }
    if (bytecode == null) {
      return -1;
    }
    return bytecode.findBytecodeIndex(frame, prev);
  }

  @Override
  protected boolean isCaptureFramesForTrace(boolean compiled) {
    return !compiled;
  }

  @Override
  public BytecodeNode getBytecodeNode() {
    return bytecode;
  }

  private AbstractBytecodeNode getBytecodeNodeImpl() {
    return bytecode;
  }

  private GettingStartedBytecodeRootNodeGen getBytecodeRootNodeImpl(int index) {
    return (GettingStartedBytecodeRootNodeGen) this.nodes.getNode(index);
  }

  @Override
  public BytecodeRootNodes<GettingStartedBytecodeRootNode> getRootNodes() {
    return this.nodes;
  }

  @Override
  protected boolean countsTowardsStackTraceLimit() {
    return true;
  }

  @Override
  public SourceSection getSourceSection() {
    return bytecode.getSourceSection();
  }

  @Override
  protected Object translateStackTraceElement(TruffleStackTraceElement stackTraceElement) {
    return AbstractBytecodeNode.createStackTraceElement(stackTraceElement);
  }

  @Override
  protected int computeSize() {
    return bytecode.bytecodes.length / 6 /* median instruction length */;
  }

  public static com.oracle.truffle.api.bytecode.BytecodeConfig.Builder newConfigBuilder() {
    return BytecodeConfig.newBuilder(BytecodeConfigEncoderImpl.INSTANCE);
  }

  private static int encodeTags(Class<?>... tags) {
    if (tags == null) {
      return 0;
    }
    int tagMask = 0;
    for (Class<?> tag : tags) {
      tagMask |= CLASS_TO_TAG_MASK.get(tag);
    }
    return tagMask;
  }

  /**
   * Creates one or more bytecode nodes. This is the entrypoint for creating new {@link
   * GettingStartedBytecodeRootNodeGen} instances.
   *
   * @param language the Truffle language instance.
   * @param config indicates whether to parse metadata (e.g., source information).
   * @param parser the parser that invokes a series of builder instructions to generate bytecode.
   */
  public static BytecodeRootNodes<GettingStartedBytecodeRootNode> create(
      BytecodeDSLTestLanguage language,
      BytecodeConfig config,
      BytecodeParser<website.lihan.temu.GettingStartedBytecodeRootNodeGen.Builder> parser) {
    BytecodeRootNodesImpl nodes = new BytecodeRootNodesImpl(parser, config);
    Builder builder = new Builder(language, nodes, config);
    parser.parse(builder);
    builder.finish();
    return nodes;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Class<? extends Tag>[] mapTagMaskToTagsArray(int tagMask) {
    ArrayList<Class<? extends Tag>> tags = new ArrayList<>();
    if ((tagMask & 1) != 0) {
      tags.add(RootTag.class);
    }
    if ((tagMask & 2) != 0) {
      tags.add(RootBodyTag.class);
    }
    if ((tagMask & 4) != 0) {
      tags.add(ExpressionTag.class);
    }
    if ((tagMask & 8) != 0) {
      tags.add(StatementTag.class);
    }
    return tags.toArray(new Class[tags.size()]);
  }

  private static ClassValue<Integer> initializeTagMaskToClass() {
    return new ClassValue<>() {
      protected Integer computeValue(Class<?> type) {
        if (type == RootTag.class) {
          return 1;
        } else if (type == RootBodyTag.class) {
          return 2;
        } else if (type == ExpressionTag.class) {
          return 4;
        } else if (type == StatementTag.class) {
          return 8;
        }
        throw new IllegalArgumentException(
            String.format(
                "Invalid tag specified. Tag '%s' not provided by language 'website.lihan.temu.BytecodeDSLTestLanguage'.",
                type.getName()));
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static <E extends Throwable> RuntimeException sneakyThrow(Throwable e) throws E {
    throw (E) e;
  }

  @TruffleBoundary
  private static AssertionError assertionFailed(String message) {
    throw new AssertionError(message);
  }

  private static byte[] unquickenBytecode(byte[] original) {
    byte[] copy = Arrays.copyOf(original, original.length);
    int bci = 0;
    while (bci < copy.length) {
      switch (BYTES.getShort(copy, bci)) {
        case Instructions.DUP:
        case Instructions.INVALIDATE0:
        case Instructions.LOAD_NULL:
        case Instructions.POP:
        case Instructions.RETURN:
        case Instructions.THROW:
          bci += 2;
          break;
        case Instructions.CLEAR_LOCAL:
        case Instructions.INVALIDATE1:
        case Instructions.LOAD_ARGUMENT:
        case Instructions.LOAD_EXCEPTION:
        case Instructions.LOAD_LOCAL:
        case Instructions.STORE_LOCAL:
          bci += 4;
          break;
        case Instructions.BRANCH:
        case Instructions.ADD_:
        case Instructions.ARRAY_INDEX_:
        case Instructions.ARRAY_LENGTH_:
        case Instructions.DIV_:
        case Instructions.EAGER_OR_:
        case Instructions.EQUALS_:
        case Instructions.LESS_THAN_:
        case Instructions.TO_BOOL_:
        case Instructions.INVALIDATE2:
        case Instructions.LOAD_CONSTANT:
          bci += 6;
          break;
        case Instructions.INVALIDATE3:
          bci += 8;
          break;
        case Instructions.BRANCH_BACKWARD:
        case Instructions.BRANCH_FALSE:
        case Instructions.INVALIDATE4:
        case Instructions.SC_OR_:
          bci += 10;
          break;
      }
    }
    return copy;
  }

  private static final class InstructionImpl extends Instruction {

    final AbstractBytecodeNode bytecode;
    final int bci;
    final int opcode;
    final byte[] bytecodes;
    final Object[] constants;

    InstructionImpl(
        AbstractBytecodeNode bytecode, int bci, int opcode, byte[] bytecodes, Object[] constants) {
      super(BytecodeRootNodesImpl.VISIBLE_TOKEN);
      this.bytecode = bytecode;
      this.bci = bci;
      this.opcode = opcode;
      this.bytecodes = bytecodes;
      this.constants = constants;
    }

    InstructionImpl(AbstractBytecodeNode bytecode, int bci, int opcode) {
      super(BytecodeRootNodesImpl.VISIBLE_TOKEN);
      this.bytecode = bytecode;
      this.bci = bci;
      this.opcode = opcode;
      this.bytecodes = bytecode.bytecodes;
      this.constants = bytecode.constants;
    }

    @Override
    public int getBytecodeIndex() {
      return bci;
    }

    @Override
    public BytecodeNode getBytecodeNode() {
      return bytecode;
    }

    @Override
    public int getOperationCode() {
      return opcode;
    }

    @Override
    public int getLength() {
      return Instructions.getLength(opcode);
    }

    @Override
    public String getName() {
      return Instructions.getName(opcode);
    }

    @Override
    public List<Argument> getArguments() {
      return Instructions.getArguments(opcode, bci, bytecode, this.bytecodes, this.constants);
    }

    @Override
    public boolean isInstrumentation() {
      return Instructions.isInstrumentation(opcode);
    }

    @Override
    protected Instruction next() {
      int nextBci = getNextBytecodeIndex();
      if (nextBci >= bytecode.bytecodes.length) {
        return null;
      }
      return new InstructionImpl(
          bytecode, nextBci, bytecode.readValidBytecode(bytecode.bytecodes, nextBci));
    }
  }

  private abstract static sealed class AbstractBytecodeNode extends BytecodeNode
      permits CachedBytecodeNode, UninitializedBytecodeNode {

    @CompilationFinal(dimensions = 1)
    final byte[] bytecodes;

    @CompilationFinal(dimensions = 1)
    final Object[] constants;

    @CompilationFinal(dimensions = 1)
    final int[] handlers;

    @CompilationFinal(dimensions = 1)
    final int[] locals;

    @CompilationFinal(dimensions = 1)
    final int[] sourceInfo;

    final List<Source> sources;
    final int numNodes;
    volatile byte[] oldBytecodes;

    protected AbstractBytecodeNode(
        byte[] bytecodes,
        Object[] constants,
        int[] handlers,
        int[] locals,
        int[] sourceInfo,
        List<Source> sources,
        int numNodes) {
      super(BytecodeRootNodesImpl.VISIBLE_TOKEN);
      this.bytecodes = bytecodes;
      this.constants = constants;
      this.handlers = handlers;
      this.locals = locals;
      this.sourceInfo = sourceInfo;
      this.sources = sources;
      this.numNodes = numNodes;
    }

    @Override
    protected abstract int findBytecodeIndex(Frame frame, Node operationNode);

    final int readValidBytecode(byte[] bc, int bci) {
      int op = BYTES.getShort(bc, bci);
      switch (op) {
        case Instructions.INVALIDATE0:
        case Instructions.INVALIDATE1:
        case Instructions.INVALIDATE2:
        case Instructions.INVALIDATE3:
        case Instructions.INVALIDATE4:
          // While we were processing the exception handler the code invalidated.
          // We need to re-read the op from the old bytecodes.
          CompilerDirectives.transferToInterpreterAndInvalidate();
          return BYTES.getShort(oldBytecodes, bci);
        default:
          return op;
      }
    }

    abstract long continueAt(
        GettingStartedBytecodeRootNodeGen $root, VirtualFrame frame, long startState);

    final GettingStartedBytecodeRootNodeGen getRoot() {
      return (GettingStartedBytecodeRootNodeGen) getParent();
    }

    abstract AbstractBytecodeNode toCached();

    abstract AbstractBytecodeNode update(
        byte[] bytecodes_,
        Object[] constants_,
        int[] handlers_,
        int[] locals_,
        int[] sourceInfo_,
        List<Source> sources_,
        int numNodes_);

    final void invalidate(AbstractBytecodeNode newNode, CharSequence reason) {
      byte[] bc = this.bytecodes;
      int bci = 0;
      this.oldBytecodes = Arrays.copyOf(bc, bc.length);
      VarHandle.loadLoadFence();
      while (bci < bc.length) {
        short op = BYTES.getShort(bc, bci);
        switch (op) {
          case Instructions.POP:
          case Instructions.DUP:
          case Instructions.LOAD_NULL:
          case Instructions.RETURN:
          case Instructions.THROW:
          case Instructions.INVALIDATE0:
            BYTES.putShort(bc, bci, Instructions.INVALIDATE0);
            bci += 2;
            break;
          case Instructions.LOAD_ARGUMENT:
          case Instructions.LOAD_LOCAL:
          case Instructions.CLEAR_LOCAL:
          case Instructions.STORE_LOCAL:
          case Instructions.LOAD_EXCEPTION:
          case Instructions.INVALIDATE1:
            BYTES.putShort(bc, bci, Instructions.INVALIDATE1);
            bci += 4;
            break;
          case Instructions.LOAD_CONSTANT:
          case Instructions.BRANCH:
          case Instructions.ADD_:
          case Instructions.DIV_:
          case Instructions.EQUALS_:
          case Instructions.LESS_THAN_:
          case Instructions.EAGER_OR_:
          case Instructions.TO_BOOL_:
          case Instructions.ARRAY_LENGTH_:
          case Instructions.ARRAY_INDEX_:
          case Instructions.INVALIDATE2:
            BYTES.putShort(bc, bci, Instructions.INVALIDATE2);
            bci += 6;
            break;
          case Instructions.INVALIDATE3:
            BYTES.putShort(bc, bci, Instructions.INVALIDATE3);
            bci += 8;
            break;
          case Instructions.BRANCH_BACKWARD:
          case Instructions.BRANCH_FALSE:
          case Instructions.SC_OR_:
          case Instructions.INVALIDATE4:
            BYTES.putShort(bc, bci, Instructions.INVALIDATE4);
            bci += 10;
            break;
        }
      }
      reportReplace(this, newNode, reason);
    }

    private final boolean validateBytecodes() {
      GettingStartedBytecodeRootNodeGen root;
      byte[] bc = this.bytecodes;
      if (bc == null) {
        // bc is null for serialization root nodes.
        return true;
      }
      Node[] cachedNodes = getCachedNodes();
      int[] branchProfiles = getBranchProfiles();
      int bci = 0;
      if (bc.length == 0) {
        throw CompilerDirectives.shouldNotReachHere(
            String.format(
                "Bytecode validation error: bytecode array must not be null%n%s",
                dumpInvalid(findLocation(bci))));
      }
      while (bci < bc.length) {
        try {
          switch (BYTES.getShort(bc, bci)) {
            case Instructions.LOAD_ARGUMENT:
              {
                bci = bci + 4;
                break;
              }
            case Instructions.LOAD_CONSTANT:
              {
                int constant = BYTES.getIntUnaligned(bc, bci + 2 /* imm constant */);
                if (constant < 0 || constant >= constants.length) {
                  throw CompilerDirectives.shouldNotReachHere(
                      String.format(
                          "Bytecode validation error at index: %s. constant is out of bounds%n%s",
                          bci, dumpInvalid(findLocation(bci))));
                }
                bci = bci + 6;
                break;
              }
            case Instructions.LOAD_LOCAL:
            case Instructions.CLEAR_LOCAL:
            case Instructions.STORE_LOCAL:
              {
                short frame_index = BYTES.getShort(bc, bci + 2 /* imm frame_index */);
                root = this.getRoot();
                if (frame_index < USER_LOCALS_START_INDEX || frame_index >= root.maxLocals) {
                  throw CompilerDirectives.shouldNotReachHere(
                      String.format(
                          "Bytecode validation error at index: %s. local offset is out of bounds%n%s",
                          bci, dumpInvalid(findLocation(bci))));
                }
                bci = bci + 4;
                break;
              }
            case Instructions.BRANCH:
              {
                int branch_target = BYTES.getIntUnaligned(bc, bci + 2 /* imm branch_target */);
                if (branch_target < 0 || branch_target >= bc.length) {
                  throw CompilerDirectives.shouldNotReachHere(
                      String.format(
                          "Bytecode validation error at index: %s. bytecode index is out of bounds%n%s",
                          bci, dumpInvalid(findLocation(bci))));
                }
                bci = bci + 6;
                break;
              }
            case Instructions.BRANCH_BACKWARD:
              {
                int branch_target = BYTES.getIntUnaligned(bc, bci + 2 /* imm branch_target */);
                if (branch_target < 0 || branch_target >= bc.length) {
                  throw CompilerDirectives.shouldNotReachHere(
                      String.format(
                          "Bytecode validation error at index: %s. bytecode index is out of bounds%n%s",
                          bci, dumpInvalid(findLocation(bci))));
                }
                int loop_header_branch_profile =
                    BYTES.getIntUnaligned(bc, bci + 6 /* imm loop_header_branch_profile */);
                if (branchProfiles != null) {
                  if (loop_header_branch_profile < 0
                      || loop_header_branch_profile >= branchProfiles.length) {
                    throw CompilerDirectives.shouldNotReachHere(
                        String.format(
                            "Bytecode validation error at index: %s. branch profile is out of bounds%n%s",
                            bci, dumpInvalid(findLocation(bci))));
                  }
                }
                bci = bci + 10;
                break;
              }
            case Instructions.BRANCH_FALSE:
            case Instructions.SC_OR_:
              {
                int branch_target = BYTES.getIntUnaligned(bc, bci + 2 /* imm branch_target */);
                if (branch_target < 0 || branch_target >= bc.length) {
                  throw CompilerDirectives.shouldNotReachHere(
                      String.format(
                          "Bytecode validation error at index: %s. bytecode index is out of bounds%n%s",
                          bci, dumpInvalid(findLocation(bci))));
                }
                int branch_profile = BYTES.getIntUnaligned(bc, bci + 6 /* imm branch_profile */);
                if (branchProfiles != null) {
                  if (branch_profile < 0 || branch_profile >= branchProfiles.length) {
                    throw CompilerDirectives.shouldNotReachHere(
                        String.format(
                            "Bytecode validation error at index: %s. branch profile is out of bounds%n%s",
                            bci, dumpInvalid(findLocation(bci))));
                  }
                }
                bci = bci + 10;
                break;
              }
            case Instructions.POP:
            case Instructions.DUP:
            case Instructions.LOAD_NULL:
            case Instructions.RETURN:
            case Instructions.THROW:
            case Instructions.INVALIDATE0:
              {
                bci = bci + 2;
                break;
              }
            case Instructions.LOAD_EXCEPTION:
              {
                short exception_sp = BYTES.getShort(bc, bci + 2 /* imm exception_sp */);
                root = this.getRoot();
                int maxStackHeight = root.getFrameDescriptor().getNumberOfSlots() - root.maxLocals;
                if (exception_sp < 0 || exception_sp > maxStackHeight) {
                  throw CompilerDirectives.shouldNotReachHere(
                      String.format(
                          "Bytecode validation error at index: %s. stack pointer is out of bounds%n%s",
                          bci, dumpInvalid(findLocation(bci))));
                }
                bci = bci + 4;
                break;
              }
            case Instructions.ADD_:
            case Instructions.DIV_:
            case Instructions.EQUALS_:
            case Instructions.LESS_THAN_:
            case Instructions.EAGER_OR_:
            case Instructions.TO_BOOL_:
            case Instructions.ARRAY_LENGTH_:
            case Instructions.ARRAY_INDEX_:
              {
                int node = BYTES.getIntUnaligned(bc, bci + 2 /* imm node */);
                if (node < 0 || node >= numNodes) {
                  throw CompilerDirectives.shouldNotReachHere(
                      String.format(
                          "Bytecode validation error at index: %s. node profile is out of bounds%n%s",
                          bci, dumpInvalid(findLocation(bci))));
                }
                bci = bci + 6;
                break;
              }
            case Instructions.INVALIDATE1:
              {
                bci = bci + 4;
                break;
              }
            case Instructions.INVALIDATE2:
              {
                bci = bci + 6;
                break;
              }
            case Instructions.INVALIDATE3:
              {
                bci = bci + 8;
                break;
              }
            case Instructions.INVALIDATE4:
              {
                bci = bci + 10;
                break;
              }
            default:
              throw CompilerDirectives.shouldNotReachHere("Invalid BCI at index: " + bci);
          }
        } catch (AssertionError e) {
          throw e;
        } catch (Throwable e) {
          throw CompilerDirectives.shouldNotReachHere(
              String.format("Bytecode validation error:%n%s", dumpInvalid(findLocation(bci))), e);
        }
      }
      int[] ex = this.handlers;
      if (ex.length % EXCEPTION_HANDLER_LENGTH != 0) {
        throw CompilerDirectives.shouldNotReachHere(
            String.format(
                "Bytecode validation error: exception handler table size is incorrect%n%s",
                dumpInvalid(findLocation(bci))));
      }
      for (int i = 0; i < ex.length; i = i + EXCEPTION_HANDLER_LENGTH) {
        int startBci = ex[i + EXCEPTION_HANDLER_OFFSET_START_BCI];
        int endBci = ex[i + EXCEPTION_HANDLER_OFFSET_END_BCI];
        int handlerKind = ex[i + EXCEPTION_HANDLER_OFFSET_KIND];
        int handlerBci = ex[i + EXCEPTION_HANDLER_OFFSET_HANDLER_BCI];
        int handlerSp = ex[i + EXCEPTION_HANDLER_OFFSET_HANDLER_SP];
        if (startBci < 0 || startBci >= bc.length) {
          throw CompilerDirectives.shouldNotReachHere(
              String.format(
                  "Bytecode validation error: exception handler startBci is out of bounds%n%s",
                  dumpInvalid(findLocation(bci))));
        }
        if (endBci < 0 || endBci > bc.length) {
          throw CompilerDirectives.shouldNotReachHere(
              String.format(
                  "Bytecode validation error: exception handler endBci is out of bounds%n%s",
                  dumpInvalid(findLocation(bci))));
        }
        if (startBci > endBci) {
          throw CompilerDirectives.shouldNotReachHere(
              String.format(
                  "Bytecode validation error: exception handler bci range is malformed%n%s",
                  dumpInvalid(findLocation(bci))));
        }
        switch (handlerKind) {
          default:
            if (handlerKind != HANDLER_CUSTOM) {
              throw CompilerDirectives.shouldNotReachHere(
                  String.format(
                      "Bytecode validation error: unexpected handler kind%n%s",
                      dumpInvalid(findLocation(bci))));
            }
            if (handlerBci < 0 || handlerBci >= bc.length) {
              throw CompilerDirectives.shouldNotReachHere(
                  String.format(
                      "Bytecode validation error: exception handler handlerBci is out of bounds%n%s",
                      dumpInvalid(findLocation(bci))));
            }
            break;
        }
      }
      int[] info = this.sourceInfo;
      List<Source> localSources = this.sources;
      if (info != null) {
        for (int i = 0; i < info.length; i += SOURCE_INFO_LENGTH) {
          int startBci = info[i + SOURCE_INFO_OFFSET_START_BCI];
          int endBci = info[i + SOURCE_INFO_OFFSET_END_BCI];
          int sourceIndex = info[i + SOURCE_INFO_OFFSET_SOURCE];
          if (startBci > endBci) {
            throw CompilerDirectives.shouldNotReachHere(
                String.format(
                    "Bytecode validation error: source bci range is malformed%n%s",
                    dumpInvalid(findLocation(bci))));
          } else if (sourceIndex < 0 || sourceIndex > localSources.size()) {
            throw CompilerDirectives.shouldNotReachHere(
                String.format(
                    "Bytecode validation error: source index is out of bounds%n%s",
                    dumpInvalid(findLocation(bci))));
          }
        }
      }
      return true;
    }

    private final String dumpInvalid(BytecodeLocation highlightedLocation) {
      try {
        return dump(highlightedLocation);
      } catch (Throwable t) {
        return "<dump error>";
      }
    }

    abstract AbstractBytecodeNode cloneUninitialized();

    abstract Node[] getCachedNodes();

    abstract int[] getBranchProfiles();

    @Override
    @TruffleBoundary
    public SourceSection getSourceSection() {
      int[] info = this.sourceInfo;
      if (info == null || info.length == 0) {
        return null;
      }
      int lastEntry = info.length - SOURCE_INFO_LENGTH;
      if (info[lastEntry + SOURCE_INFO_OFFSET_START_BCI] == 0
          && info[lastEntry + SOURCE_INFO_OFFSET_END_BCI] == bytecodes.length) {
        return createSourceSection(sources, info, lastEntry);
      }
      return null;
    }

    @Override
    public final SourceSection getSourceLocation(int bci) {
      assert validateBytecodeIndex(bci);
      int[] info = this.sourceInfo;
      if (info == null) {
        return null;
      }
      for (int i = 0; i < info.length; i += SOURCE_INFO_LENGTH) {
        int startBci = info[i + SOURCE_INFO_OFFSET_START_BCI];
        int endBci = info[i + SOURCE_INFO_OFFSET_END_BCI];
        if (startBci <= bci && bci < endBci) {
          return createSourceSection(sources, info, i);
        }
      }
      return null;
    }

    @Override
    public final SourceSection[] getSourceLocations(int bci) {
      assert validateBytecodeIndex(bci);
      int[] info = this.sourceInfo;
      if (info == null) {
        return null;
      }
      int sectionIndex = 0;
      SourceSection[] sections = new SourceSection[8];
      for (int i = 0; i < info.length; i += SOURCE_INFO_LENGTH) {
        int startBci = info[i + SOURCE_INFO_OFFSET_START_BCI];
        int endBci = info[i + SOURCE_INFO_OFFSET_END_BCI];
        if (startBci <= bci && bci < endBci) {
          if (sectionIndex == sections.length) {
            sections =
                Arrays.copyOf(
                    sections, Math.min(sections.length * 2, info.length / SOURCE_INFO_LENGTH));
          }
          sections[sectionIndex++] = createSourceSection(sources, info, i);
        }
      }
      return Arrays.copyOf(sections, sectionIndex);
    }

    @Override
    protected Instruction findInstruction(int bci) {
      return new InstructionImpl(this, bci, readValidBytecode(this.bytecodes, bci));
    }

    @Override
    protected boolean validateBytecodeIndex(int bci) {
      byte[] bc = this.bytecodes;
      if (bci < 0 || bci >= bc.length) {
        throw new IllegalArgumentException("Bytecode index out of range " + bci);
      }
      int op = readValidBytecode(bc, bci);
      if (op < 0 || op > 28) {
        throw new IllegalArgumentException("Invalid op at bytecode index " + op);
      }
      return true;
    }

    @Override
    public List<SourceInformation> getSourceInformation() {
      if (sourceInfo == null) {
        return null;
      }
      return new SourceInformationList(this);
    }

    @Override
    public boolean hasSourceInformation() {
      return sourceInfo != null;
    }

    @Override
    public SourceInformationTree getSourceInformationTree() {
      if (sourceInfo == null) {
        return null;
      }
      return SourceInformationTreeImpl.parse(this);
    }

    @Override
    public List<ExceptionHandler> getExceptionHandlers() {
      return new ExceptionHandlerList(this);
    }

    @Override
    public TagTree getTagTree() {
      return null;
    }

    @Override
    @ExplodeLoop
    public final int getLocalCount(int bci) {
      assert validateBytecodeIndex(bci);
      CompilerAsserts.partialEvaluationConstant(bci);
      int count = 0;
      for (int index = 0; index < locals.length; index += LOCALS_LENGTH) {
        int startIndex = locals[index + LOCALS_OFFSET_START_BCI];
        int endIndex = locals[index + LOCALS_OFFSET_END_BCI];
        if (bci >= startIndex && bci < endIndex) {
          count++;
        }
      }
      CompilerAsserts.partialEvaluationConstant(count);
      return count;
    }

    @Override
    protected final void clearLocalValueInternal(Frame frame, int localOffset, int localIndex) {
      assert getRoot().getFrameDescriptor() == frame.getFrameDescriptor()
          : "Invalid frame with invalid descriptor passed.";
      int frameIndex = USER_LOCALS_START_INDEX + localOffset;
      frame.clear(frameIndex);
    }

    @Override
    protected final boolean isLocalClearedInternal(Frame frame, int localOffset, int localIndex) {
      assert getRoot().getFrameDescriptor() == frame.getFrameDescriptor()
          : "Invalid frame with invalid descriptor passed.";
      int frameIndex = USER_LOCALS_START_INDEX + localOffset;
      return frame.getTag(frameIndex) == FrameSlotKind.Illegal.tag;
    }

    @Override
    protected Object getLocalNameInternal(int localOffset, int localIndex) {
      int index = localIndexToAnyTableIndex(localIndex);
      int nameId = locals[index + LOCALS_OFFSET_NAME];
      if (nameId == -1) {
        return null;
      } else {
        return ACCESS.readObject(this.constants, nameId);
      }
    }

    @Override
    protected Object getLocalInfoInternal(int localOffset, int localIndex) {
      int index = localIndexToAnyTableIndex(localIndex);
      int infoId = locals[index + LOCALS_OFFSET_INFO];
      if (infoId == -1) {
        return null;
      } else {
        return ACCESS.readObject(this.constants, infoId);
      }
    }

    @Override
    public final Object getLocalValue(int bci, Frame frame, int localOffset) {
      assert validateBytecodeIndex(bci);
      CompilerAsserts.partialEvaluationConstant(bci);
      CompilerAsserts.partialEvaluationConstant(localOffset);
      assert localOffset >= 0 && localOffset < getLocalCount(bci)
          : "Invalid out-of-bounds local offset provided.";
      if (getRoot().getFrameDescriptor() != frame.getFrameDescriptor()) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new IllegalArgumentException("Invalid frame with invalid descriptor passed.");
      }
      int frameIndex = USER_LOCALS_START_INDEX + localOffset;
      if (frame.isObject(frameIndex)) {
        return frame.getObject(frameIndex);
      }
      return null;
    }

    @Override
    public void setLocalValue(int bci, Frame frame, int localOffset, Object value) {
      assert validateBytecodeIndex(bci);
      CompilerAsserts.partialEvaluationConstant(bci);
      CompilerAsserts.partialEvaluationConstant(localOffset);
      assert localOffset >= 0 && localOffset < getLocalCount(bci)
          : "Invalid out-of-bounds local offset provided.";
      if (getRoot().getFrameDescriptor() != frame.getFrameDescriptor()) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new IllegalArgumentException("Invalid frame with invalid descriptor passed.");
      }
      int frameIndex = USER_LOCALS_START_INDEX + localOffset;
      frame.setObject(frameIndex, value);
    }

    @Override
    protected final Object getLocalValueInternal(Frame frame, int localOffset, int localIndex) {
      assert getRoot().getFrameDescriptor() == frame.getFrameDescriptor()
          : "Invalid frame with invalid descriptor passed.";
      int frameIndex = USER_LOCALS_START_INDEX + localOffset;
      return frame.getObject(frameIndex);
    }

    @Override
    protected void setLocalValueInternal(
        Frame frame, int localOffset, int localIndex, Object value) {
      assert getRoot().getFrameDescriptor() == frame.getFrameDescriptor()
          : "Invalid frame with invalid descriptor passed.";
      frame.setObject(USER_LOCALS_START_INDEX + localOffset, value);
    }

    @ExplodeLoop
    protected final int localOffsetToTableIndex(int bci, int localOffset) {
      int count = 0;
      for (int index = 0; index < locals.length; index += LOCALS_LENGTH) {
        int startIndex = locals[index + LOCALS_OFFSET_START_BCI];
        int endIndex = locals[index + LOCALS_OFFSET_END_BCI];
        if (bci >= startIndex && bci < endIndex) {
          if (count == localOffset) {
            return index;
          }
          count++;
        }
      }
      return -1;
    }

    protected final int localOffsetToLocalIndex(int bci, int localOffset) {
      int tableIndex = localOffsetToTableIndex(bci, localOffset);
      assert locals[tableIndex + LOCALS_OFFSET_FRAME_INDEX] == localOffset + USER_LOCALS_START_INDEX
          : "Inconsistent indices.";
      return locals[tableIndex + LOCALS_OFFSET_LOCAL_INDEX];
    }

    @ExplodeLoop
    protected final int localIndexToAnyTableIndex(int localIndex) {
      for (int index = 0; index < locals.length; index += LOCALS_LENGTH) {
        if (locals[index + LOCALS_OFFSET_LOCAL_INDEX] == localIndex) {
          return index;
        }
      }
      CompilerDirectives.transferToInterpreterAndInvalidate();
      throw new AssertionError("Local index not found in locals table");
    }

    @Override
    public Object getLocalName(int bci, int localOffset) {
      assert validateBytecodeIndex(bci);
      CompilerAsserts.partialEvaluationConstant(bci);
      CompilerAsserts.partialEvaluationConstant(localOffset);
      assert localOffset >= 0 && localOffset < getLocalCount(bci)
          : "Invalid out-of-bounds local offset provided.";
      int index = localOffsetToTableIndex(bci, localOffset);
      if (index == -1) {
        return null;
      }
      int nameId = locals[index + LOCALS_OFFSET_NAME];
      if (nameId == -1) {
        return null;
      } else {
        return ACCESS.readObject(this.constants, nameId);
      }
    }

    @Override
    public Object getLocalInfo(int bci, int localOffset) {
      assert validateBytecodeIndex(bci);
      CompilerAsserts.partialEvaluationConstant(bci);
      CompilerAsserts.partialEvaluationConstant(localOffset);
      assert localOffset >= 0 && localOffset < getLocalCount(bci)
          : "Invalid out-of-bounds local offset provided.";
      int index = localOffsetToTableIndex(bci, localOffset);
      if (index == -1) {
        return null;
      }
      int infoId = locals[index + LOCALS_OFFSET_INFO];
      if (infoId == -1) {
        return null;
      } else {
        return ACCESS.readObject(this.constants, infoId);
      }
    }

    @Override
    public List<LocalVariable> getLocals() {
      return new LocalVariableList(this);
    }

    @Override
    protected int translateBytecodeIndex(BytecodeNode newNode, int bytecodeIndex) {
      return (int) transitionState((AbstractBytecodeNode) newNode, (bytecodeIndex & 0xFFFFFFFFL));
    }

    final long transitionState(AbstractBytecodeNode newBytecode, long state) {
      byte[] oldBc = this.oldBytecodes;
      byte[] newBc = newBytecode.bytecodes;
      if (oldBc == null || this == newBytecode || this.bytecodes == newBc) {
        // No change in bytecodes.
        return state;
      }
      int oldBci = (int) state;
      int newBci = computeNewBci(oldBci, oldBc, newBc);
      return (state & 0xFFFF00000000L) | (newBci & 0xFFFFFFFFL);
    }

    public void adoptNodesAfterUpdate() {
      // no nodes to adopt
    }

    static BytecodeLocation findLocation(AbstractBytecodeNode node, int bci) {
      return node.findLocation(bci);
    }

    private static SourceSection createSourceSection(List<Source> sources, int[] info, int index) {
      int sourceIndex = info[index + SOURCE_INFO_OFFSET_SOURCE];
      int start = info[index + SOURCE_INFO_OFFSET_START];
      int length = info[index + SOURCE_INFO_OFFSET_LENGTH];
      if (start == -1 && length == -1) {
        return sources.get(sourceIndex).createUnavailableSection();
      }
      assert start >= 0 : "invalid source start index";
      assert length >= 0 : "invalid source length";
      return sources.get(sourceIndex).createSection(start, length);
    }

    private static int toStableBytecodeIndex(byte[] bc, int searchBci) {
      int bci = 0;
      int stableBci = 0;
      while (bci != searchBci && bci < bc.length) {
        switch (BYTES.getShort(bc, bci)) {
          case Instructions.POP:
          case Instructions.DUP:
          case Instructions.LOAD_NULL:
          case Instructions.RETURN:
          case Instructions.THROW:
          case Instructions.INVALIDATE0:
            bci += 2;
            stableBci += 2;
            break;
          case Instructions.LOAD_ARGUMENT:
          case Instructions.LOAD_LOCAL:
          case Instructions.CLEAR_LOCAL:
          case Instructions.STORE_LOCAL:
          case Instructions.LOAD_EXCEPTION:
          case Instructions.INVALIDATE1:
            bci += 4;
            stableBci += 4;
            break;
          case Instructions.LOAD_CONSTANT:
          case Instructions.BRANCH:
          case Instructions.ADD_:
          case Instructions.DIV_:
          case Instructions.EQUALS_:
          case Instructions.LESS_THAN_:
          case Instructions.EAGER_OR_:
          case Instructions.TO_BOOL_:
          case Instructions.ARRAY_LENGTH_:
          case Instructions.ARRAY_INDEX_:
          case Instructions.INVALIDATE2:
            bci += 6;
            stableBci += 6;
            break;
          case Instructions.INVALIDATE3:
            bci += 8;
            stableBci += 8;
            break;
          case Instructions.BRANCH_BACKWARD:
          case Instructions.BRANCH_FALSE:
          case Instructions.SC_OR_:
          case Instructions.INVALIDATE4:
            bci += 10;
            stableBci += 10;
            break;
          default:
            throw CompilerDirectives.shouldNotReachHere("Invalid bytecode.");
        }
      }
      if (bci >= bc.length) {
        throw CompilerDirectives.shouldNotReachHere("Could not translate bytecode index.");
      }
      return stableBci;
    }

    private static int fromStableBytecodeIndex(byte[] bc, int stableSearchBci) {
      int bci = 0;
      int stableBci = 0;
      while (stableBci != stableSearchBci && bci < bc.length) {
        switch (BYTES.getShort(bc, bci)) {
          case Instructions.POP:
          case Instructions.DUP:
          case Instructions.LOAD_NULL:
          case Instructions.RETURN:
          case Instructions.THROW:
          case Instructions.INVALIDATE0:
            bci += 2;
            stableBci += 2;
            break;
          case Instructions.LOAD_ARGUMENT:
          case Instructions.LOAD_LOCAL:
          case Instructions.CLEAR_LOCAL:
          case Instructions.STORE_LOCAL:
          case Instructions.LOAD_EXCEPTION:
          case Instructions.INVALIDATE1:
            bci += 4;
            stableBci += 4;
            break;
          case Instructions.LOAD_CONSTANT:
          case Instructions.BRANCH:
          case Instructions.ADD_:
          case Instructions.DIV_:
          case Instructions.EQUALS_:
          case Instructions.LESS_THAN_:
          case Instructions.EAGER_OR_:
          case Instructions.TO_BOOL_:
          case Instructions.ARRAY_LENGTH_:
          case Instructions.ARRAY_INDEX_:
          case Instructions.INVALIDATE2:
            bci += 6;
            stableBci += 6;
            break;
          case Instructions.INVALIDATE3:
            bci += 8;
            stableBci += 8;
            break;
          case Instructions.BRANCH_BACKWARD:
          case Instructions.BRANCH_FALSE:
          case Instructions.SC_OR_:
          case Instructions.INVALIDATE4:
            bci += 10;
            stableBci += 10;
            break;
          default:
            throw CompilerDirectives.shouldNotReachHere("Invalid bytecode.");
        }
      }
      if (bci >= bc.length) {
        throw CompilerDirectives.shouldNotReachHere("Could not translate bytecode index.");
      }
      return bci;
    }

    private static int transitionInstrumentationIndex(
        byte[] oldBc, int oldBciBase, int oldBciTarget, byte[] newBc, int newBciBase) {
      int oldBci = oldBciBase;
      int newBci = newBciBase;
      short searchOp = -1;
      while (oldBci < oldBciTarget) {
        short op = BYTES.getShort(oldBc, oldBci);
        searchOp = op;
        switch (op) {
          default:
            throw CompilerDirectives.shouldNotReachHere("Unexpected bytecode.");
        }
      }
      assert searchOp != -1;
      oldBci = oldBciBase;
      int opCounter = 0;
      while (oldBci < oldBciTarget) {
        short op = BYTES.getShort(oldBc, oldBci);
        switch (op) {
          default:
            throw CompilerDirectives.shouldNotReachHere("Unexpected bytecode.");
        }
      }
      assert opCounter > 0;
      while (opCounter > 0) {
        short op = BYTES.getShort(newBc, newBci);
        switch (op) {
          default:
            throw CompilerDirectives.shouldNotReachHere("Unexpected bytecode.");
        }
      }
      return newBci;
    }

    static final int computeNewBci(int oldBci, byte[] oldBc, byte[] newBc) {
      int stableBci = toStableBytecodeIndex(oldBc, oldBci);
      int newBci = fromStableBytecodeIndex(newBc, stableBci);
      int oldBciBase = fromStableBytecodeIndex(oldBc, stableBci);
      if (oldBci != oldBciBase) {
        // Transition within an in instrumentation bytecode.
        // Needs to compute exact location where to continue.
        newBci = transitionInstrumentationIndex(oldBc, oldBciBase, oldBci, newBc, newBci);
      }
      return newBci;
    }

    private static Object createStackTraceElement(TruffleStackTraceElement stackTraceElement) {
      return createDefaultStackTraceElement(stackTraceElement);
    }
  }

  @DenyReplace
  private static final class CachedBytecodeNode extends AbstractBytecodeNode
      implements BytecodeOSRNode {

    private static final boolean[] EMPTY_EXCEPTION_PROFILES = new boolean[0];

    @CompilationFinal(dimensions = 1)
    private final Node[] cachedNodes_;

    @CompilationFinal(dimensions = 1)
    private final boolean[] exceptionProfiles_;

    @CompilationFinal(dimensions = 1)
    private final int[] branchProfiles_;

    @CompilationFinal private Object osrMetadata_;

    CachedBytecodeNode(
        byte[] bytecodes,
        Object[] constants,
        int[] handlers,
        int[] locals,
        int[] sourceInfo,
        List<Source> sources,
        int numNodes) {
      super(bytecodes, constants, handlers, locals, sourceInfo, sources, numNodes);
      CompilerAsserts.neverPartOfCompilation();
      Node[] result = new Node[this.numNodes];
      byte[] bc = bytecodes;
      int bci = 0;
      int numConditionalBranches = 0;
      loop:
      while (bci < bc.length) {
        switch (BYTES.getShort(bc, bci)) {
          case Instructions.POP:
          case Instructions.DUP:
          case Instructions.LOAD_NULL:
          case Instructions.RETURN:
          case Instructions.THROW:
          case Instructions.INVALIDATE0:
            bci += 2;
            break;
          case Instructions.LOAD_ARGUMENT:
          case Instructions.LOAD_LOCAL:
          case Instructions.CLEAR_LOCAL:
          case Instructions.STORE_LOCAL:
          case Instructions.LOAD_EXCEPTION:
          case Instructions.INVALIDATE1:
            bci += 4;
            break;
          case Instructions.LOAD_CONSTANT:
          case Instructions.BRANCH:
          case Instructions.INVALIDATE2:
            bci += 6;
            break;
          case Instructions.INVALIDATE3:
            bci += 8;
            break;
          case Instructions.BRANCH_BACKWARD:
          case Instructions.INVALIDATE4:
            bci += 10;
            break;
          case Instructions.ADD_:
            result[BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)] = insert(new Add_Node());
            bci += 6;
            break;
          case Instructions.DIV_:
            result[BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)] = insert(new Div_Node());
            bci += 6;
            break;
          case Instructions.EQUALS_:
            result[BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)] = insert(new Equals_Node());
            bci += 6;
            break;
          case Instructions.LESS_THAN_:
            result[BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)] = insert(new LessThan_Node());
            bci += 6;
            break;
          case Instructions.EAGER_OR_:
            result[BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)] = insert(new EagerOr_Node());
            bci += 6;
            break;
          case Instructions.TO_BOOL_:
            result[BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)] = insert(new ToBool_Node());
            bci += 6;
            break;
          case Instructions.ARRAY_LENGTH_:
            result[BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)] =
                insert(new ArrayLength_Node());
            bci += 6;
            break;
          case Instructions.ARRAY_INDEX_:
            result[BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)] =
                insert(new ArrayIndex_Node());
            bci += 6;
            break;
          case Instructions.BRANCH_FALSE:
          case Instructions.SC_OR_:
            numConditionalBranches++;
            bci += 10;
            break;
          default:
            {
              throw assertionFailed("Should not reach here");
            }
        }
      }
      assert bci == bc.length;
      this.cachedNodes_ = result;
      this.branchProfiles_ = allocateBranchProfiles(numConditionalBranches);
      this.exceptionProfiles_ =
          handlers.length == 0 ? EMPTY_EXCEPTION_PROFILES : new boolean[handlers.length / 5];
    }

    CachedBytecodeNode(
        byte[] bytecodes,
        Object[] constants,
        int[] handlers,
        int[] locals,
        int[] sourceInfo,
        List<Source> sources,
        int numNodes,
        Node[] cachedNodes_,
        boolean[] exceptionProfiles_,
        int[] branchProfiles_,
        Object osrMetadata_) {
      super(bytecodes, constants, handlers, locals, sourceInfo, sources, numNodes);
      this.cachedNodes_ = cachedNodes_;
      this.exceptionProfiles_ = exceptionProfiles_;
      this.branchProfiles_ = branchProfiles_;
      this.osrMetadata_ = osrMetadata_;
    }

    @Override
    @BytecodeInterpreterSwitch
    @ExplodeLoop(kind = LoopExplosionKind.MERGE_EXPLODE)
    long continueAt(GettingStartedBytecodeRootNodeGen $root, VirtualFrame frame_, long startState) {
      VirtualFrame frame = ACCESS.uncheckedCast(frame_, FRAME_TYPE);
      byte[] bc = ACCESS.uncheckedCast(this.bytecodes, byte[].class);
      if (HostCompilerDirectives.inInterpreterFastPath()) {
        // Force constants and cached nodes being read outside of the loop with fences.
        Reference.reachabilityFence(ACCESS.uncheckedCast(this.constants, Object[].class));
        Reference.reachabilityFence(ACCESS.uncheckedCast(this.cachedNodes_, Node[].class));
      }
      int bci = (int) startState;
      int sp = (short) (startState >>> 32);
      int counter = 0;
      LoopCounter loopCounter = null;
      if (CompilerDirectives.hasNextTier() && !CompilerDirectives.inInterpreter()) {
        // Using a class for the loop counter is a workaround to prevent PE from merging it at the
        // end of the loop.
        // We need to use a class with PE, in the interpreter we can use a regular counter.
        loopCounter = new LoopCounter();
      }
      loop:
      while (true) {
        CompilerAsserts.partialEvaluationConstant(bci);
        try {
          switch (BYTES.getShort(bc, bci)) {
            case Instructions.LOAD_ARGUMENT:
              {
                FRAMES.setObject(
                    frame, sp, frame.getArguments()[BYTES.getShort(bc, bci + 2 /* imm index */)]);
                sp += 1;
                bci += 4;
                break;
              }
            case Instructions.LOAD_CONSTANT:
              {
                if (CompilerDirectives.inCompiledCode()) {
                  loadConstantCompiled(frame, bc, bci, sp);
                } else {
                  FRAMES.setObject(
                      frame,
                      sp,
                      ACCESS.readObject(
                          ACCESS.uncheckedCast(this.constants, Object[].class),
                          BYTES.getIntUnaligned(bc, bci + 2 /* imm constant */)));
                }
                sp += 1;
                bci += 6;
                break;
              }
            case Instructions.LOAD_LOCAL:
              {
                doLoadLocal(frame, bc, bci, sp);
                sp += 1;
                bci += 4;
                break;
              }
            case Instructions.CLEAR_LOCAL:
              {
                FRAMES.clear(frame, BYTES.getShort(bc, bci + 2 /* imm frame_index */));
                bci += 4;
                break;
              }
            case Instructions.STORE_LOCAL:
              {
                doStoreLocal(frame, bc, bci, sp);
                sp -= 1;
                bci += 4;
                break;
              }
            case Instructions.BRANCH:
              {
                bci = BYTES.getIntUnaligned(bc, bci + 2 /* imm branch_target */);
                break;
              }
            case Instructions.BRANCH_BACKWARD:
              {
                TruffleSafepoint.poll(this);
                if (CompilerDirectives.hasNextTier()) {
                  if (CompilerDirectives.inCompiledCode()) {
                    counter = ++loopCounter.value;
                  } else {
                    counter++;
                  }
                  if (CompilerDirectives.injectBranchProbability(
                      LoopCounter.REPORT_LOOP_PROBABILITY,
                      counter >= LoopCounter.REPORT_LOOP_STRIDE)) {
                    Object osrResult = reportLoopCount(frame, bc, bci, sp, counter);
                    if (osrResult != null) {
                      return (long) osrResult;
                    }
                    if (CompilerDirectives.inCompiledCode()) {
                      loopCounter.value = 0;
                    } else {
                      counter = 0;
                    }
                  }
                  if (CompilerDirectives.inCompiledCode()) {
                    counter = 0;
                  }
                }
                bci = BYTES.getIntUnaligned(bc, bci + 2 /* imm branch_target */);
                break;
              }
            case Instructions.BRANCH_FALSE:
              {
                if ((boolean) FRAMES.uncheckedGetObject(frame, sp - 1)) {
                  bci += 10;
                } else {
                  bci = BYTES.getIntUnaligned(bc, bci + 2 /* imm branch_target */);
                }
                sp -= 1;
                break;
              }
            case Instructions.POP:
              {
                doPop(frame, bc, bci, sp);
                sp -= 1;
                bci += 2;
                break;
              }
            case Instructions.DUP:
              {
                FRAMES.copy(frame, sp - 1, sp);
                sp += 1;
                bci += 2;
                break;
              }
            case Instructions.LOAD_NULL:
              {
                FRAMES.setObject(frame, sp, null);
                sp += 1;
                bci += 2;
                break;
              }
            case Instructions.RETURN:
              {
                if (CompilerDirectives.hasNextTier()) {
                  if (CompilerDirectives.inCompiledCode()) {
                    counter = loopCounter.value;
                  }
                  if (counter > 0) {
                    LoopNode.reportLoopCount(this, counter);
                  }
                }
                return (((sp - 1) & 0xFFFFL) << 32) | 0xFFFFFFFFL;
              }
            case Instructions.THROW:
              {
                throw sneakyThrow((Throwable) FRAMES.uncheckedGetObject(frame, sp - 1));
              }
            case Instructions.LOAD_EXCEPTION:
              {
                FRAMES.setObject(
                    frame,
                    sp,
                    FRAMES.getObject(
                        frame,
                        $root.maxLocals + BYTES.getShort(bc, bci + 2 /* imm exception_sp */)));
                sp += 1;
                bci += 4;
                break;
              }
            case Instructions.ADD_:
              {
                doAdd_(frame, bc, bci, sp);
                sp -= 1;
                bci += 6;
                break;
              }
            case Instructions.DIV_:
              {
                doDiv_(frame, bc, bci, sp);
                sp -= 1;
                bci += 6;
                break;
              }
            case Instructions.EQUALS_:
              {
                doEquals_(frame, bc, bci, sp);
                sp -= 1;
                bci += 6;
                break;
              }
            case Instructions.LESS_THAN_:
              {
                doLessThan_(frame, bc, bci, sp);
                sp -= 1;
                bci += 6;
                break;
              }
            case Instructions.EAGER_OR_:
              {
                doEagerOr_(frame, bc, bci, sp);
                sp -= 1;
                bci += 6;
                break;
              }
            case Instructions.TO_BOOL_:
              {
                doToBool_(frame, bc, bci, sp);
                bci += 6;
                break;
              }
            case Instructions.ARRAY_LENGTH_:
              {
                doArrayLength_(frame, bc, bci, sp);
                bci += 6;
                break;
              }
            case Instructions.ARRAY_INDEX_:
              {
                doArrayIndex_(frame, bc, bci, sp);
                sp -= 1;
                bci += 6;
                break;
              }
            case Instructions.SC_OR_:
              {
                if (profileBranch(
                    BYTES.getIntUnaligned(bc, bci + 6 /* imm branch_profile */),
                    (boolean) FRAMES.uncheckedGetObject(frame, sp - 1))) {
                  bci = BYTES.getIntUnaligned(bc, bci + 2 /* imm branch_target */);
                  break;
                } else {
                  FRAMES.clear(frame, sp - 1);
                  sp -= 1;
                  bci += 10;
                  break;
                }
              }
            case Instructions.INVALIDATE0:
              {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return ((sp & 0xFFFFL) << 32) | (bci & 0xFFFFFFFFL);
              }
            case Instructions.INVALIDATE1:
              {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return ((sp & 0xFFFFL) << 32) | (bci & 0xFFFFFFFFL);
              }
            case Instructions.INVALIDATE2:
              {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return ((sp & 0xFFFFL) << 32) | (bci & 0xFFFFFFFFL);
              }
            case Instructions.INVALIDATE3:
              {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return ((sp & 0xFFFFL) << 32) | (bci & 0xFFFFFFFFL);
              }
            case Instructions.INVALIDATE4:
              {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return ((sp & 0xFFFFL) << 32) | (bci & 0xFFFFFFFFL);
              }
          }
        } catch (Throwable throwable) {
          int targetSp = sp;
          throwable = resolveThrowable($root, frame, bci, throwable);
          int handler = -EXCEPTION_HANDLER_LENGTH;
          while ((handler = resolveHandler(bci, handler + EXCEPTION_HANDLER_LENGTH, this.handlers))
              != -1) {
            if (throwable instanceof java.lang.ThreadDeath) {
              continue;
            }
            assert throwable instanceof AbstractTruffleException;
            bci = this.handlers[handler + EXCEPTION_HANDLER_OFFSET_HANDLER_BCI];
            targetSp =
                this.handlers[handler + EXCEPTION_HANDLER_OFFSET_HANDLER_SP] + $root.maxLocals;
            FRAMES.setObject(frame, targetSp - 1, throwable);
            assert sp >= targetSp - 1;
            while (sp > targetSp) {
              FRAMES.clear(frame, --sp);
            }
            sp = targetSp;
            continue loop;
          }
          if (CompilerDirectives.hasNextTier()) {
            if (CompilerDirectives.inCompiledCode()) {
              counter = loopCounter.value;
            }
            if (counter > 0) {
              LoopNode.reportLoopCount(this, counter);
            }
          }
          throw sneakyThrow(throwable);
        }
      }
    }

    @TruffleBoundary
    private static void print(String format, Object... args) {
      System.out.println(String.format(format, args));
    }

    private void doLoadLocal(Frame frame, byte[] bc, int bci, int sp) {
      FRAMES.setObject(
          frame,
          sp,
          FRAMES.requireObject(frame, BYTES.getShort(bc, bci + 2 /* imm frame_index */)));
    }

    private void doStoreLocal(Frame frame, byte[] bc, int bci, int sp) {
      Object local = FRAMES.requireObject(frame, sp - 1);
      FRAMES.setObject(frame, BYTES.getShort(bc, bci + 2 /* imm frame_index */), local);
      FRAMES.clear(frame, sp - 1);
    }

    private Object reportLoopCount(VirtualFrame frame, byte[] bc, int bci, int sp, int counter) {
      LoopNode.reportLoopCount(this, counter);
      if (CompilerDirectives.inInterpreter() && BytecodeOSRNode.pollOSRBackEdge(this, counter)) {
        int branchProfileIndex =
            BYTES.getIntUnaligned(bc, bci + 6 /* imm loop_header_branch_profile */);
        ensureFalseProfile(
            ACCESS.uncheckedCast(this.branchProfiles_, int[].class), branchProfileIndex);
        return BytecodeOSRNode.tryOSR(
            this,
            ((sp & 0xFFFFL) << 32)
                | (BYTES.getIntUnaligned(bc, bci + 2 /* imm branch_target */) & 0xFFFFFFFFL),
            null,
            null,
            frame);
      }
      return null;
    }

    private void doAdd_(VirtualFrame frame, byte[] bc, int bci, int sp) {
      Add_Node node =
          ACCESS.uncheckedCast(
              ACCESS.readObject(
                  ACCESS.uncheckedCast(this.cachedNodes_, Node[].class),
                  BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)),
              Add_Node.class);
      int result = node.execute(frame, this, bc, bci, sp);
      FRAMES.setObject(frame, sp - 2, result);
      if (CompilerDirectives.inCompiledCode()) {
        FRAMES.clear(frame, sp - 1);
      }
    }

    private void doDiv_(VirtualFrame frame, byte[] bc, int bci, int sp) {
      Div_Node node =
          ACCESS.uncheckedCast(
              ACCESS.readObject(
                  ACCESS.uncheckedCast(this.cachedNodes_, Node[].class),
                  BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)),
              Div_Node.class);
      int result = node.execute(frame, this, bc, bci, sp);
      FRAMES.setObject(frame, sp - 2, result);
      if (CompilerDirectives.inCompiledCode()) {
        FRAMES.clear(frame, sp - 1);
      }
    }

    private void doEquals_(VirtualFrame frame, byte[] bc, int bci, int sp) {
      Equals_Node node =
          ACCESS.uncheckedCast(
              ACCESS.readObject(
                  ACCESS.uncheckedCast(this.cachedNodes_, Node[].class),
                  BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)),
              Equals_Node.class);
      boolean result = node.execute(frame, this, bc, bci, sp);
      FRAMES.setObject(frame, sp - 2, result);
      if (CompilerDirectives.inCompiledCode()) {
        FRAMES.clear(frame, sp - 1);
      }
    }

    private void doLessThan_(VirtualFrame frame, byte[] bc, int bci, int sp) {
      LessThan_Node node =
          ACCESS.uncheckedCast(
              ACCESS.readObject(
                  ACCESS.uncheckedCast(this.cachedNodes_, Node[].class),
                  BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)),
              LessThan_Node.class);
      boolean result = node.execute(frame, this, bc, bci, sp);
      FRAMES.setObject(frame, sp - 2, result);
      if (CompilerDirectives.inCompiledCode()) {
        FRAMES.clear(frame, sp - 1);
      }
    }

    private void doEagerOr_(VirtualFrame frame, byte[] bc, int bci, int sp) {
      EagerOr_Node node =
          ACCESS.uncheckedCast(
              ACCESS.readObject(
                  ACCESS.uncheckedCast(this.cachedNodes_, Node[].class),
                  BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)),
              EagerOr_Node.class);
      boolean result = node.execute(frame, this, bc, bci, sp);
      FRAMES.setObject(frame, sp - 2, result);
      if (CompilerDirectives.inCompiledCode()) {
        FRAMES.clear(frame, sp - 1);
      }
    }

    private void doToBool_(VirtualFrame frame, byte[] bc, int bci, int sp) {
      ToBool_Node node =
          ACCESS.uncheckedCast(
              ACCESS.readObject(
                  ACCESS.uncheckedCast(this.cachedNodes_, Node[].class),
                  BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)),
              ToBool_Node.class);
      boolean result = node.execute(frame, this, bc, bci, sp);
      FRAMES.setObject(frame, sp - 1, result);
    }

    private void doArrayLength_(VirtualFrame frame, byte[] bc, int bci, int sp) {
      ArrayLength_Node node =
          ACCESS.uncheckedCast(
              ACCESS.readObject(
                  ACCESS.uncheckedCast(this.cachedNodes_, Node[].class),
                  BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)),
              ArrayLength_Node.class);
      int result = node.execute(frame, this, bc, bci, sp);
      FRAMES.setObject(frame, sp - 1, result);
    }

    private void doArrayIndex_(VirtualFrame frame, byte[] bc, int bci, int sp) {
      ArrayIndex_Node node =
          ACCESS.uncheckedCast(
              ACCESS.readObject(
                  ACCESS.uncheckedCast(this.cachedNodes_, Node[].class),
                  BYTES.getIntUnaligned(bc, bci + 2 /* imm node */)),
              ArrayIndex_Node.class);
      int result = node.execute(frame, this, bc, bci, sp);
      FRAMES.setObject(frame, sp - 2, result);
      FRAMES.clear(frame, sp - 1);
    }

    private void loadConstantCompiled(VirtualFrame frame, byte[] bc, int bci, int sp) {
      Object constant =
          ACCESS.readObject(
              ACCESS.uncheckedCast(this.constants, Object[].class),
              BYTES.getIntUnaligned(bc, bci + 2 /* imm constant */));
      if (constant instanceof Boolean b) {
        FRAMES.setObject(frame, sp, b.booleanValue());
        return;
      } else if (constant instanceof Byte b) {
        FRAMES.setObject(frame, sp, b.byteValue());
        return;
      } else if (constant instanceof Character c) {
        FRAMES.setObject(frame, sp, c.charValue());
        return;
      } else if (constant instanceof Float f) {
        FRAMES.setObject(frame, sp, f.floatValue());
        return;
      } else if (constant instanceof Integer i) {
        FRAMES.setObject(frame, sp, i.intValue());
        return;
      } else if (constant instanceof Long l) {
        FRAMES.setObject(frame, sp, l.longValue());
        return;
      } else if (constant instanceof Short s) {
        FRAMES.setObject(frame, sp, s.shortValue());
        return;
      } else if (constant instanceof Double d) {
        FRAMES.setObject(frame, sp, d.doubleValue());
        return;
      }
      FRAMES.setObject(frame, sp, constant);
    }

    @Override
    public void adoptNodesAfterUpdate() {
      insert(this.cachedNodes_);
    }

    private boolean profileBranch(int profileIndex, boolean condition) {
      int[] branchProfiles = ACCESS.uncheckedCast(this.branchProfiles_, int[].class);
      int t;
      int f;
      if (HostCompilerDirectives.inInterpreterFastPath()) {
        if (condition) {
          t = ACCESS.readInt(branchProfiles, profileIndex * 2);
          if (t == 0) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
          }
          try {
            t = Math.addExact(t, 1);
          } catch (ArithmeticException e) {
            f = ACCESS.readInt(branchProfiles, profileIndex * 2 + 1);
            // shift count but never make it go to 0
            f = (f & 0x1) + (f >> 1);
            ACCESS.writeInt(branchProfiles, profileIndex * 2 + 1, f);
            t = Integer.MAX_VALUE >> 1;
          }
          ACCESS.writeInt(branchProfiles, profileIndex * 2, t);
        } else {
          f = ACCESS.readInt(branchProfiles, profileIndex * 2 + 1);
          if (f == 0) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
          }
          try {
            f = Math.addExact(f, 1);
          } catch (ArithmeticException e) {
            t = ACCESS.readInt(branchProfiles, profileIndex * 2);
            // shift count but never make it go to 0
            t = (t & 0x1) + (t >> 1);
            ACCESS.writeInt(branchProfiles, profileIndex * 2, t);
            f = Integer.MAX_VALUE >> 1;
          }
          ACCESS.writeInt(branchProfiles, profileIndex * 2 + 1, f);
        }
        return condition;
      } else {
        t = ACCESS.readInt(branchProfiles, profileIndex * 2);
        f = ACCESS.readInt(branchProfiles, profileIndex * 2 + 1);
        if (condition) {
          if (t == 0) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
          }
          if (f == 0) {
            return true;
          }
        } else {
          if (f == 0) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
          }
          if (t == 0) {
            return false;
          }
        }
        return CompilerDirectives.injectBranchProbability((double) t / (double) (t + f), condition);
      }
    }

    @Override
    public Object executeOSR(VirtualFrame frame, long target, Object unused) {
      return continueAt(getRoot(), frame, target);
    }

    @Override
    public void prepareOSR(long target) {
      // do nothing
    }

    @Override
    public void copyIntoOSRFrame(
        VirtualFrame osrFrame, VirtualFrame parentFrame, long target, Object targetMetadata) {
      transferOSRFrame(osrFrame, parentFrame, target, targetMetadata);
    }

    @Override
    public Object getOSRMetadata() {
      return osrMetadata_;
    }

    @Override
    public void setOSRMetadata(Object osrMetadata) {
      osrMetadata_ = osrMetadata;
    }

    @Override
    public Object[] storeParentFrameInArguments(VirtualFrame parentFrame) {
      Object[] parentArgs = parentFrame.getArguments();
      Object[] result = Arrays.copyOf(parentArgs, parentArgs.length + 1);
      result[result.length - 1] = parentFrame;
      return result;
    }

    @Override
    public Frame restoreParentFrameFromArguments(Object[] arguments) {
      return (Frame) arguments[arguments.length - 1];
    }

    @Override
    public void setUncachedThreshold(int threshold) {}

    @Override
    public BytecodeTier getTier() {
      return BytecodeTier.CACHED;
    }

    @InliningCutoff
    private Throwable resolveThrowable(
        GettingStartedBytecodeRootNodeGen $root, VirtualFrame frame, int bci, Throwable throwable) {
      if (throwable instanceof AbstractTruffleException ate) {
        return ate;
      } else if (throwable instanceof ControlFlowException cfe) {
        throw cfe;
      } else {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw sneakyThrow(throwable);
      }
    }

    @ExplodeLoop
    private int resolveHandler(int bci, int handler, int[] localHandlers) {
      int handlerEntryIndex = Math.floorDiv(handler, EXCEPTION_HANDLER_LENGTH);
      for (int i = handler;
          i < localHandlers.length;
          i += EXCEPTION_HANDLER_LENGTH, handlerEntryIndex++) {
        if (localHandlers[i + EXCEPTION_HANDLER_OFFSET_START_BCI] > bci) {
          continue;
        }
        if (localHandlers[i + EXCEPTION_HANDLER_OFFSET_END_BCI] <= bci) {
          continue;
        }
        if (!this.exceptionProfiles_[handlerEntryIndex]) {
          CompilerDirectives.transferToInterpreterAndInvalidate();
          this.exceptionProfiles_[handlerEntryIndex] = true;
        }
        return i;
      }
      return -1;
    }

    @Override
    AbstractBytecodeNode toCached() {
      return this;
    }

    @Override
    AbstractBytecodeNode update(
        byte[] bytecodes_,
        Object[] constants_,
        int[] handlers_,
        int[] locals_,
        int[] sourceInfo_,
        List<Source> sources_,
        int numNodes_) {
      assert bytecodes_ != null || sourceInfo_ != null;
      byte[] bytecodes__;
      Object[] constants__;
      int[] handlers__;
      int[] locals__;
      int[] sourceInfo__;
      List<Source> sources__;
      int numNodes__;
      if (bytecodes_ != null) {
        bytecodes__ = bytecodes_;
        constants__ = constants_;
        handlers__ = handlers_;
        numNodes__ = numNodes_;
        locals__ = locals_;
      } else {
        bytecodes__ = this.bytecodes;
        constants__ = this.constants;
        handlers__ = this.handlers;
        numNodes__ = this.numNodes;
        locals__ = this.locals;
      }
      if (sourceInfo_ != null) {
        sourceInfo__ = sourceInfo_;
        sources__ = sources_;
      } else {
        sourceInfo__ = this.sourceInfo;
        sources__ = this.sources;
      }
      if (bytecodes_ != null) {
        // Can't reuse profile if bytecodes are changed.
        return new CachedBytecodeNode(
            bytecodes__, constants__, handlers__, locals__, sourceInfo__, sources__, numNodes__);
      } else {
        // Can reuse profile if bytecodes are unchanged.
        return new CachedBytecodeNode(
            bytecodes__,
            constants__,
            handlers__,
            locals__,
            sourceInfo__,
            sources__,
            numNodes__,
            this.cachedNodes_,
            this.exceptionProfiles_,
            this.branchProfiles_,
            this.osrMetadata_);
      }
    }

    @Override
    AbstractBytecodeNode cloneUninitialized() {
      return new CachedBytecodeNode(
          unquickenBytecode(this.bytecodes),
          this.constants,
          this.handlers,
          this.locals,
          this.sourceInfo,
          this.sources,
          this.numNodes);
    }

    @Override
    Node[] getCachedNodes() {
      return this.cachedNodes_;
    }

    @Override
    int[] getBranchProfiles() {
      return this.branchProfiles_;
    }

    @Override
    @TruffleBoundary
    protected int findBytecodeIndex(FrameInstance frameInstance) {
      Node prev = null;
      for (Node current = frameInstance.getCallNode();
          current != null;
          current = current.getParent()) {
        if (current == this && prev != null) {
          return findBytecodeIndexOfOperationNode(prev);
        }
        prev = current;
      }
      return -1;
    }

    @Override
    protected int findBytecodeIndex(Frame frame, Node node) {
      if (node != null) {
        return findBytecodeIndexOfOperationNode(node);
      }
      return -1;
    }

    @TruffleBoundary
    int findBytecodeIndexOfOperationNode(Node operationNode) {
      assert operationNode.getParent() == this
          : "Passed node must be an operation node of the same bytecode node.";
      Node[] localNodes = this.cachedNodes_;
      byte[] bc = this.bytecodes;
      int bci = 0;
      loop:
      while (bci < bc.length) {
        int currentBci = bci;
        int nodeIndex;
        switch (BYTES.getShort(bc, bci)) {
          case Instructions.LOAD_ARGUMENT:
          case Instructions.LOAD_LOCAL:
          case Instructions.CLEAR_LOCAL:
          case Instructions.STORE_LOCAL:
          case Instructions.LOAD_EXCEPTION:
          case Instructions.INVALIDATE1:
            {
              bci += 4;
              continue loop;
            }
          case Instructions.LOAD_CONSTANT:
          case Instructions.BRANCH:
          case Instructions.INVALIDATE2:
            {
              bci += 6;
              continue loop;
            }
          case Instructions.BRANCH_BACKWARD:
          case Instructions.BRANCH_FALSE:
          case Instructions.SC_OR_:
          case Instructions.INVALIDATE4:
            {
              bci += 10;
              continue loop;
            }
          case Instructions.POP:
          case Instructions.DUP:
          case Instructions.LOAD_NULL:
          case Instructions.RETURN:
          case Instructions.THROW:
          case Instructions.INVALIDATE0:
            {
              bci += 2;
              continue loop;
            }
          case Instructions.INVALIDATE3:
            {
              bci += 8;
              continue loop;
            }
          case Instructions.ADD_:
          case Instructions.DIV_:
          case Instructions.EQUALS_:
          case Instructions.LESS_THAN_:
          case Instructions.EAGER_OR_:
          case Instructions.TO_BOOL_:
          case Instructions.ARRAY_LENGTH_:
          case Instructions.ARRAY_INDEX_:
            {
              nodeIndex = BYTES.getIntUnaligned(bc, bci + 2 /* imm node */);
              bci += 6;
              break;
            }
          default:
            {
              throw assertionFailed("Should not reach here");
            }
        }
        if (localNodes[nodeIndex] == operationNode) {
          return currentBci;
        }
      }
      return -1;
    }

    @Override
    public String toString() {
      return String.format(
          "BytecodeNode [name=%s, sources=%s, tier=cached]",
          ((RootNode) getParent()).getQualifiedName(), this.sourceInfo != null);
    }

    private static void doPop(Frame frame, byte[] bc, int bci, int sp) {
      FRAMES.clear(frame, sp - 1);
    }

    private static int[] allocateBranchProfiles(int numProfiles) {
      // Encoding: [t1, f1, t2, f2, ..., tn, fn]
      return new int[numProfiles * 2];
    }

    private static void ensureFalseProfile(int[] branchProfiles, int profileIndex) {
      if (ACCESS.readInt(branchProfiles, profileIndex * 2 + 1) == 0) {
        ACCESS.writeInt(branchProfiles, profileIndex * 2 + 1, 1);
      }
    }
  }

  @DenyReplace
  private static final class UninitializedBytecodeNode extends AbstractBytecodeNode {

    UninitializedBytecodeNode(
        byte[] bytecodes,
        Object[] constants,
        int[] handlers,
        int[] locals,
        int[] sourceInfo,
        List<Source> sources,
        int numNodes) {
      super(bytecodes, constants, handlers, locals, sourceInfo, sources, numNodes);
    }

    @Override
    @BytecodeInterpreterSwitch
    long continueAt(GettingStartedBytecodeRootNodeGen $root, VirtualFrame frame_, long startState) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      $root.transitionToCached();
      return startState;
    }

    @Override
    public void setUncachedThreshold(int threshold) {}

    @Override
    public BytecodeTier getTier() {
      return BytecodeTier.UNCACHED;
    }

    @Override
    AbstractBytecodeNode toCached() {
      return new CachedBytecodeNode(
          this.bytecodes,
          this.constants,
          this.handlers,
          this.locals,
          this.sourceInfo,
          this.sources,
          this.numNodes);
    }

    @Override
    AbstractBytecodeNode update(
        byte[] bytecodes_,
        Object[] constants_,
        int[] handlers_,
        int[] locals_,
        int[] sourceInfo_,
        List<Source> sources_,
        int numNodes_) {
      assert bytecodes_ != null || sourceInfo_ != null;
      byte[] bytecodes__;
      Object[] constants__;
      int[] handlers__;
      int[] locals__;
      int[] sourceInfo__;
      List<Source> sources__;
      int numNodes__;
      if (bytecodes_ != null) {
        bytecodes__ = bytecodes_;
        constants__ = constants_;
        handlers__ = handlers_;
        numNodes__ = numNodes_;
        locals__ = locals_;
      } else {
        bytecodes__ = this.bytecodes;
        constants__ = this.constants;
        handlers__ = this.handlers;
        numNodes__ = this.numNodes;
        locals__ = this.locals;
      }
      if (sourceInfo_ != null) {
        sourceInfo__ = sourceInfo_;
        sources__ = sources_;
      } else {
        sourceInfo__ = this.sourceInfo;
        sources__ = this.sources;
      }
      return new UninitializedBytecodeNode(
          bytecodes__, constants__, handlers__, locals__, sourceInfo__, sources__, numNodes__);
    }

    @Override
    AbstractBytecodeNode cloneUninitialized() {
      return new UninitializedBytecodeNode(
          Arrays.copyOf(this.bytecodes, this.bytecodes.length),
          this.constants,
          this.handlers,
          this.locals,
          this.sourceInfo,
          this.sources,
          this.numNodes);
    }

    @Override
    Node[] getCachedNodes() {
      return null;
    }

    @Override
    int[] getBranchProfiles() {
      return null;
    }

    @Override
    @TruffleBoundary
    protected int findBytecodeIndex(FrameInstance frameInstance) {
      return -1;
    }

    @Override
    protected int findBytecodeIndex(Frame frame, Node node) {
      return -1;
    }

    int findBytecodeIndexOfOperationNode(Node operationNode) {
      return -1;
    }

    @Override
    public String toString() {
      return String.format(
          "BytecodeNode [name=%s, sources=%s, tier=uninitialized]",
          ((RootNode) getParent()).getQualifiedName(), this.sourceInfo != null);
    }
  }

  /**
   * Builder class to generate bytecode. An interpreter can invoke this class with its {@link
   * com.oracle.truffle.api.bytecode.BytecodeParser} to generate bytecode.
   */
  public static final class Builder extends BytecodeBuilder {

    private static final byte UNINITIALIZED = -1;
    private static final int PATCH_CURRENT_SOURCE = -2;
    private static final int PATCH_NODE_SOURCE = -3;

    private final BytecodeDSLTestLanguage language;
    private final BytecodeRootNodesImpl nodes;
    private final CharSequence reparseReason;
    private final boolean parseBytecodes;
    private final int tags;
    private final int instrumentations;
    private final boolean parseSources;
    private final ArrayList<GettingStartedBytecodeRootNodeGen> builtNodes;
    private final ArrayList<Source> sources;
    private RootStackElement state;
    private int numRoots;

    /** Constructor for initial parses. */
    private Builder(
        BytecodeDSLTestLanguage language, BytecodeRootNodesImpl nodes, BytecodeConfig config) {
      super(BytecodeRootNodesImpl.VISIBLE_TOKEN);
      this.language = language;
      this.nodes = nodes;
      this.reparseReason = null;
      long encoding = BytecodeConfigEncoderImpl.decode(config);
      this.tags = (int) ((encoding >> 32) & 0xFFFF_FFFF);
      this.instrumentations = (int) ((encoding >> 1) & 0x7FFF_FFFF);
      this.parseSources = (encoding & 0x1) != 0;
      this.parseBytecodes = true;
      this.sources = parseSources ? new ArrayList<>(4) : null;
      this.builtNodes = new ArrayList<>();
      this.state = RootStackElement.acquire();
    }

    /** Constructor for reparsing. */
    private Builder(
        BytecodeRootNodesImpl nodes,
        boolean parseBytecodes,
        int tags,
        int instrumentations,
        boolean parseSources,
        CharSequence reparseReason) {
      super(BytecodeRootNodesImpl.VISIBLE_TOKEN);
      this.language = nodes.getLanguage();
      this.nodes = nodes;
      this.reparseReason = reparseReason;
      this.parseBytecodes = parseBytecodes;
      this.tags = tags;
      this.instrumentations = instrumentations;
      this.parseSources = parseSources;
      this.sources = parseSources ? new ArrayList<>(4) : null;
      this.builtNodes = new ArrayList<>();
      this.state = RootStackElement.acquire();
    }

    /** Creates a new local. Uses default values for the local's metadata. */
    public BytecodeLocal createLocal() {
      return createLocal(null, null);
    }

    /**
     * Creates a new local. Uses the given {@code name} and {@code info} in its local metadata.
     *
     * @param name the name assigned to the local's slot.
     * @param info the info assigned to the local's slot.
     * @see BytecodeNode#getLocalNames
     * @see BytecodeNode#getLocalInfos
     */
    public BytecodeLocal createLocal(Object name, Object info) {
      OperationStackElement scope = getCurrentScope();
      short localIndex = state.allocateBytecodeLocal() /* unique global index */;
      short frameIndex =
          safeCastShort(
              USER_LOCALS_START_INDEX
                  + scope.getFrameOffset()
                  + scope.getNumLocals()) /* location in frame */;
      int tableIndex =
          state.doEmitLocal(localIndex, frameIndex, name, info) /* index in global table */;
      scope.registerLocal(tableIndex);
      BytecodeLocalImpl local =
          new BytecodeLocalImpl(
              frameIndex,
              localIndex,
              safeCastShort(state.operationStack[state.rootOperationSp].getIndex()),
              scope,
              scope.sequenceNumber);
      return local;
    }

    /**
     * Creates a new label. The result should be {@link #emitLabel emitted} and can be {@link
     * #emitBranch branched to}.
     */
    public BytecodeLabel createLabel() {
      OperationStackElement operationStack = state.peekOperation();
      if (operationStack == null
          || (operationStack.operation != Operations.BLOCK
              && operationStack.operation != Operations.ROOT)) {
        throw failState("Labels must be created inside either Block or Root operations.");
      }
      BytecodeLabel result =
          new BytecodeLabelImpl(state.numLabels++, UNINITIALIZED, operationStack.sequenceNumber);
      operationStack.addDeclaredLabel(result);
      return result;
    }

    /**
     * Begins a built-in SourceSection operation with an unavailable source section.
     *
     * @see #beginSourceSection(int, int)
     * @see #endSourceSectionUnavailable()
     */
    public void beginSourceSectionUnavailable() {
      beginSourceSection(-1, -1);
    }

    /**
     * Ends a built-in SourceSection operation with an unavailable source section.
     *
     * @see #endSourceSection()
     * @see #beginSourceSectionUnavailable()
     */
    public void endSourceSectionUnavailable() {
      endSourceSection();
    }

    /**
     * Begins a built-in Block operation.
     *
     * <p>Signature: Block(body...) -> void/Object
     *
     * <p>Block is a grouping operation that executes each child in its body sequentially, producing
     * the result of the last child (if any). This operation can be used to group multiple
     * operations together in a single operation. The result of a Block is the result produced by
     * the last child (or void, if no value is produced).
     *
     * <p>A corresponding call to {@link #endBlock} is required to end the operation.
     */
    public void beginBlock() {
      validateRootOperationBegin();
      OperationStackElement parentScope = getCurrentScope();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.BLOCK);
      // Initialize operation Block
      operation.setStartStackHeight(state.currentStackHeight);
      operation.setProducedValue(false);
      operation.setFrameOffset(0);
      operation.setNumLocals(0);
      operation.setValid(true);
      operation.setDeclaredLabels(null);
      operation.setFrameOffset(parentScope.getFrameOffset() + parentScope.getNumLocals());
    }

    /**
     * Ends a built-in Block operation.
     *
     * <p>Signature: Block(body...) -> void/Object
     *
     * @see #beginBlock
     */
    public void endBlock() {
      OperationStackElement operation = endOperation(Operations.BLOCK);
      if (!operation.validateDeclaredLabels()) {
        throw failState("Operation Block ended without emitting one or more declared labels.");
      }
      if (operation.getNumLocals() > 0) {
        state.maxLocals =
            Math.max(state.maxLocals, operation.getFrameOffset() + operation.getNumLocals());
        for (int index = 0; index < operation.getNumLocals(); index++) {
          state.locals[operation.getLocals()[index] + LOCALS_OFFSET_END_BCI] = state.bci;
          state.doEmitInstructionS(
              Instructions.CLEAR_LOCAL,
              0,
              safeCastShort(
                  state.locals[operation.getLocals()[index] + LOCALS_OFFSET_FRAME_INDEX]));
        }
      }
      operation.setValid(false);
      afterChild(Operations.BLOCK, operation.getProducedValue());
    }

    /**
     * Begins a new root node.
     *
     * <p>Signature: Root(body...)
     *
     * <p>Each Root operation defines one function (i.e., a {@link GettingStartedBytecodeRootNode}).
     * It takes one or more children, which define the body of the function that executes when it is
     * invoked. If control falls through to the end of the body without returning, instructions are
     * inserted to implicitly return {@code null}.
     *
     * <p>A root operation is typically the outermost one. That is, a {@link BytecodeParser} should
     * invoke {@link #beginRoot} first before using other builder methods to generate bytecode. The
     * parser should invoke {@link #endRoot} to finish generating the {@link
     * GettingStartedBytecodeRootNode}.
     *
     * <p>A parser *can* nest this operation in Source and SourceSection operations in order to
     * provide a {@link Node#getSourceSection source location} for the entire root node. The result
     * of {@link Node#getSourceSection} on the generated root is undefined if there is no enclosing
     * SourceSection operation.
     *
     * <p>This method can also be called inside of another root operation. Bytecode generation for
     * the outer root node suspends until generation for the inner root node finishes. The inner
     * root node is not lexically nested in the outer (you can invoke the inner root node
     * independently), and it does not have access to the outer root's locals (if it needs access to
     * outer locals, consider {@link
     * com.oracle.truffle.api.bytecode.GenerateBytecode#enableMaterializedLocalAccesses enabling
     * materialized local accesses}). Multiple root nodes can be obtained from the {@link
     * BytecodeNodes} object in the order of their {@link #beginRoot} calls.
     */
    public void beginRoot() {
      if (state.rootOperationSp != -1) {
        state = state.getNext();
      }
      state.rootOperationSp = state.operationSp;
      OperationStackElement operation = beginOperation(Operations.ROOT);
      // Initialize operation Root
      operation.setIndex(safeCastShort(numRoots++));
      operation.setProducedValue(false);
      operation.setReachable(true);
      operation.setFrameOffset(0);
      operation.setNumLocals(0);
      operation.setValid(true);
      operation.setDeclaredLabels(null);
      if (reparseReason == null) {
        builtNodes.add(null);
        if (builtNodes.size() > Short.MAX_VALUE) {
          throw BytecodeEncodingException.create("Root node count exceeded maximum value.");
        }
      }
      operation.setFrameOffset(state.numLocals);
    }

    /**
     * Finishes generating bytecode for the current root node.
     *
     * <p>Signature: Root(body...)
     *
     * @returns the root node with generated bytecode.
     */
    public GettingStartedBytecodeRootNode endRoot() {
      OperationStackElement operation = state.operationStack[state.rootOperationSp];
      assert operation.operation == Operations.ROOT;
      if (!operation.getProducedValue()) {
        emitLoadNull();
      }
      if (!state.operationStack[state.rootOperationSp].validateDeclaredLabels()) {
        throw failState("Operation Root ended without emitting one or more declared labels.");
      }
      state.doEmitInstruction(Instructions.RETURN, -1);
      endOperation(Operations.ROOT);
      if (operation.getNumLocals() > 0) {
        state.maxLocals =
            Math.max(state.maxLocals, operation.getFrameOffset() + operation.getNumLocals());
        for (int index = 0; index < operation.getNumLocals(); index++) {
          state.locals[operation.getLocals()[index] + LOCALS_OFFSET_END_BCI] = state.bci;
        }
      }
      operation.setValid(false);
      byte[] bytecodes_ = null;
      Object[] constants_ = null;
      int[] handlers_ = null;
      int[] locals_ = null;
      int[] sourceInfo_ = null;
      List<Source> sources_ = null;
      int numNodes_ = 0;
      doEmitRootSourceSection(operation.getIndex());
      if (parseSources) {
        sourceInfo_ = Arrays.copyOf(state.sourceInfo, state.sourceInfoIndex);
        sources_ = sources;
      }
      if (parseBytecodes) {
        bytecodes_ = Arrays.copyOf(state.bc, state.bci);
        constants_ = state.toConstants();
        handlers_ = Arrays.copyOf(state.handlerTable, state.handlerTableSize);
        sources_ = sources;
        numNodes_ = state.numNodes;
        locals_ =
            state.locals == null
                ? EMPTY_INT_ARRAY
                : Arrays.copyOf(state.locals, state.localsTableIndex);
      } else {
        state.toConstants();
      }
      GettingStartedBytecodeRootNodeGen result;
      if (reparseReason != null) {
        result = builtNodes.get(operation.getIndex());
        if (parseBytecodes) {
          AbstractBytecodeNode oldBytecodeNode = result.bytecode;
          assert result.maxLocals == state.maxLocals + USER_LOCALS_START_INDEX;
          assert result.nodes == this.nodes;
          assert constants_.length == oldBytecodeNode.constants.length;
          assert result.getFrameDescriptor().getNumberOfSlots()
              == state.maxStackHeight + state.maxLocals + USER_LOCALS_START_INDEX;
        }
        result.updateBytecode(
            bytecodes_,
            constants_,
            handlers_,
            locals_,
            sourceInfo_,
            sources_,
            numNodes_,
            this.reparseReason);
        assert result.buildIndex == operation.getIndex();
      } else {
        com.oracle.truffle.api.frame.FrameDescriptor.Builder frameDescriptorBuilder =
            FrameDescriptor.newBuilder();
        frameDescriptorBuilder.defaultValueIllegal();
        frameDescriptorBuilder.useSlotKinds(false);
        frameDescriptorBuilder.addSlots(
            state.maxStackHeight + state.maxLocals + USER_LOCALS_START_INDEX);
        result =
            new GettingStartedBytecodeRootNodeGen(
                language,
                frameDescriptorBuilder,
                nodes,
                state.maxLocals + USER_LOCALS_START_INDEX,
                operation.getIndex(),
                bytecodes_,
                constants_,
                handlers_,
                locals_,
                sourceInfo_,
                sources_,
                numNodes_);
        assert operation.getIndex() <= numRoots;
        builtNodes.set(operation.getIndex(), result);
      }
      state.reset();
      if (state.parent != null) {
        state = state.parent;
      }
      return result;
    }

    /**
     * Begins a built-in IfThen operation.
     *
     * <p>Signature: IfThen(condition, thens) -> void
     *
     * <p>IfThen implements an if-then statement. It evaluates {@code condition}, which must produce
     * a boolean. If the value is {@code true}, it executes {@code thens}. This is a void operation;
     * {@code thens} can also be void.
     *
     * <p>A corresponding call to {@link #endIfThen} is required to end the operation.
     */
    public void beginIfThen() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.IFTHEN);
      // Initialize operation IfThen
      operation.setThenReachable(state.reachable);
      operation.setFalseBranchFixupBci(UNINITIALIZED);
    }

    /**
     * Ends a built-in IfThen operation.
     *
     * <p>Signature: IfThen(condition, thens) -> void
     *
     * @see #beginIfThen
     */
    public void endIfThen() {
      OperationStackElement operation = endOperation(Operations.IFTHEN);
      if (operation.childCount != 2) {
        throw failState(
            "Operation IfThen expected exactly 2 children, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      updateReachable();
      afterChild(Operations.IFTHEN, false);
    }

    /**
     * Begins a built-in IfThenElse operation.
     *
     * <p>Signature: IfThenElse(condition, thens, elses) -> void
     *
     * <p>IfThenElse implements an if-then-else statement. It evaluates {@code condition}, which
     * must produce a boolean. If the value is {@code true}, it executes {@code thens}; otherwise,
     * it executes {@code elses}. This is a void operation; both {@code thens} and {@code elses} can
     * also be void.
     *
     * <p>A corresponding call to {@link #endIfThenElse} is required to end the operation.
     */
    public void beginIfThenElse() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.IFTHENELSE);
      // Initialize operation IfThenElse
      operation.setThenReachable(state.reachable);
      operation.setElseReachable(state.reachable);
      operation.setFalseBranchFixupBci(UNINITIALIZED);
      operation.setEndBranchFixupBci(UNINITIALIZED);
    }

    /**
     * Ends a built-in IfThenElse operation.
     *
     * <p>Signature: IfThenElse(condition, thens, elses) -> void
     *
     * @see #beginIfThenElse
     */
    public void endIfThenElse() {
      OperationStackElement operation = endOperation(Operations.IFTHENELSE);
      if (operation.childCount != 3) {
        throw failState(
            "Operation IfThenElse expected exactly 3 children, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      markReachable(operation.getThenReachable() || operation.getElseReachable());
      afterChild(Operations.IFTHENELSE, false);
    }

    /**
     * Begins a built-in Conditional operation.
     *
     * <p>Signature: Conditional(condition, thens, elses) -> Object
     *
     * <p>Conditional implements a conditional expression (e.g., {@code condition ? thens : elses}
     * in Java). It has the same semantics as IfThenElse, except it produces the value of the
     * conditionally-executed child.
     *
     * <p>A corresponding call to {@link #endConditional} is required to end the operation.
     */
    public void beginConditional() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.CONDITIONAL);
      // Initialize operation Conditional
      operation.setThenReachable(state.reachable);
      operation.setElseReachable(state.reachable);
      operation.setFalseBranchFixupBci(UNINITIALIZED);
      operation.setEndBranchFixupBci(UNINITIALIZED);
    }

    /**
     * Ends a built-in Conditional operation.
     *
     * <p>Signature: Conditional(condition, thens, elses) -> Object
     *
     * @see #beginConditional
     */
    public void endConditional() {
      OperationStackElement operation = endOperation(Operations.CONDITIONAL);
      if (operation.childCount != 3) {
        throw failState(
            "Operation Conditional expected exactly 3 children, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      markReachable(operation.getThenReachable() || operation.getElseReachable());
      afterChild(Operations.CONDITIONAL, true);
    }

    /**
     * Begins a built-in While operation.
     *
     * <p>Signature: While(condition, body) -> void
     *
     * <p>While implements a while loop. It evaluates {@code condition}, which must produce a
     * boolean. If the value is {@code true}, it executes {@code body} and repeats. This is a void
     * operation; {@code body} can also be void.
     *
     * <p>A corresponding call to {@link #endWhile} is required to end the operation.
     */
    public void beginWhile() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.WHILE);
      // Initialize operation While
      operation.setWhileStartBci(state.bci);
      operation.setBodyReachable(state.reachable);
      operation.setEndBranchFixupBci(UNINITIALIZED);
    }

    /**
     * Ends a built-in While operation.
     *
     * <p>Signature: While(condition, body) -> void
     *
     * @see #beginWhile
     */
    public void endWhile() {
      OperationStackElement operation = endOperation(Operations.WHILE);
      if (operation.childCount != 2) {
        throw failState(
            "Operation While expected exactly 2 children, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      updateReachable();
      afterChild(Operations.WHILE, false);
    }

    /**
     * Begins a built-in TryCatch operation.
     *
     * <p>Signature: TryCatch(try, catch) -> void
     *
     * <p>TryCatch implements an exception handler. It executes {@code try}, and if a Truffle
     * exception is thrown, it executes {@code catch}. The exception can be accessed within the
     * {@code catch} operation using LoadException. Unlike a Java try-catch, this operation does not
     * filter the exception based on type. This is a void operation; both {@code try} and {@code
     * catch} can also be void.
     *
     * <p>A corresponding call to {@link #endTryCatch} is required to end the operation.
     */
    public void beginTryCatch() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.TRYCATCH);
      // Initialize operation TryCatch
      operation.setHandlerId(++state.numHandlers);
      operation.setStackHeight(state.currentStackHeight);
      operation.setTryStartBci(state.bci);
      operation.setOperationReachable(state.reachable);
      operation.setTryReachable(state.reachable);
      operation.setCatchReachable(state.reachable);
      operation.setEndBranchFixupBci(UNINITIALIZED);
      operation.setExtraTableEntriesStart(UNINITIALIZED);
      operation.setExtraTableEntriesEnd(UNINITIALIZED);
    }

    /**
     * Ends a built-in TryCatch operation.
     *
     * <p>Signature: TryCatch(try, catch) -> void
     *
     * @see #beginTryCatch
     */
    public void endTryCatch() {
      OperationStackElement operation = endOperation(Operations.TRYCATCH);
      if (operation.childCount != 2) {
        throw failState(
            "Operation TryCatch expected exactly 2 children, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      markReachable(operation.getTryReachable() || operation.getCatchReachable());
      afterChild(Operations.TRYCATCH, false);
    }

    /**
     * Begins a built-in TryFinally operation.
     *
     * <p>Signature: TryFinally(try) -> void
     *
     * <p>TryFinally implements a finally handler. It executes {@code try}, and after execution
     * finishes it always executes {@code finally}. If {@code try} finishes normally, {@code
     * finally} executes and control continues after the TryFinally operation. If {@code try}
     * finishes exceptionally, {@code finally} executes and then rethrows the exception. If {@code
     * try} finishes with a control flow operation, {@code finally} executes and then the control
     * flow operation continues (i.e., a Branch will branch, a Return will return).
     *
     * <p>Unlike other child operations, {@code finally} is emitted multiple times in the bytecode
     * (once for each regular, exceptional, and early control flow exit). To facilitate this, the
     * {@code finally} operation is specified by a {@code finallyGenerator} that can be invoked
     * multiple times. It should be repeatable and not have side effects.
     *
     * <p>This is a void operation; either of {@code try} or {@code finally} can be void.
     *
     * <p>A corresponding call to {@link #endTryFinally} is required to end the operation.
     *
     * @param finallyGenerator an idempotent Runnable that generates the {@code finally} operation
     *     using builder calls.
     */
    public void beginTryFinally(Runnable finallyGenerator) {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.TRYFINALLY);
      // Initialize operation TryFinally
      operation.setHandlerId(++state.numHandlers);
      operation.setStackHeight(state.currentStackHeight);
      operation.setFinallyGenerator(finallyGenerator);
      operation.setTryStartBci(state.bci);
      operation.setOperationReachable(state.reachable);
      operation.setTryReachable(state.reachable);
      operation.setCatchReachable(false);
      operation.setEndBranchFixupBci(UNINITIALIZED);
      operation.setExtraTableEntriesStart(UNINITIALIZED);
      operation.setExtraTableEntriesEnd(UNINITIALIZED);
      operation.setFinallyHandlerSp(UNINITIALIZED);
    }

    /**
     * Ends a built-in TryFinally operation.
     *
     * <p>Signature: TryFinally(try) -> void
     *
     * @see #beginTryFinally
     */
    public void endTryFinally() {
      OperationStackElement operation = verifyOperation(Operations.TRYFINALLY);
      if (operation.childCount != 1) {
        throw failState(
            "Operation TryFinally expected exactly 1 child, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      int handlerSp = state.currentStackHeight + 1 /* reserve space for the exception */;
      state.updateMaxStackHeight(handlerSp);
      int exHandlerIndex = UNINITIALIZED;
      if (operation.getOperationReachable()) {
        // register exception table entry
        exHandlerIndex =
            state.doCreateExceptionHandler(
                operation.getTryStartBci(),
                state.bci,
                HANDLER_CUSTOM,
                -operation.getHandlerId(),
                handlerSp);
      }
      // emit handler for normal completion case
      doEmitFinallyHandler(operation, state.operationSp - 1);
      // the operation was popped, so manually update reachability. try is reachable if neither it
      // nor the finally handler exited early.
      operation.setTryReachable(operation.getTryReachable() && state.reachable);
      if (state.reachable) {
        operation.setEndBranchFixupBci(state.bci + 2);
        state.doEmitInstructionI(Instructions.BRANCH, 0, UNINITIALIZED);
      }
      if (operation.getOperationReachable()) {
        // update exception table; force handler code to be reachable
        state.reachable = true;
        state.patchHandlerTable(
            operation.getExtraTableEntriesStart(),
            operation.getExtraTableEntriesEnd(),
            operation.getHandlerId(),
            state.bci,
            handlerSp);
        if (exHandlerIndex != UNINITIALIZED) {
          state.handlerTable[exHandlerIndex + EXCEPTION_HANDLER_OFFSET_HANDLER_BCI] = state.bci;
        }
      }
      // emit handler for exceptional case
      state.currentStackHeight = handlerSp;
      doEmitFinallyHandler(operation, state.operationSp - 1);
      state.doEmitInstruction(Instructions.THROW, -1);
      if (operation.getEndBranchFixupBci() != UNINITIALIZED) {
        BYTES.putInt(state.bc, operation.getEndBranchFixupBci(), state.bci);
      }
      state.popOperation();
      markReachable(operation.getTryReachable());
      afterChild(Operations.TRYFINALLY, false);
    }

    /**
     * Begins a built-in TryCatchOtherwise operation.
     *
     * <p>Signature: TryCatchOtherwise(try, catch) -> void
     *
     * <p>TryCatchOtherwise implements a try block with different handling for regular and
     * exceptional behaviour. It executes {@code try} and then one of the handlers. If {@code try}
     * finishes normally, {@code otherwise} executes and control continues after the
     * TryCatchOtherwise operation. If {@code try} finishes exceptionally, {@code catch} executes.
     * The exception can be accessed using LoadException. Control continues after the
     * TryCatchOtherwise operation. If {@code try} finishes with a control flow operation, {@code
     * otherwise} executes and then the control flow operation continues (i.e., a Branch will
     * branch, a Return will return).
     *
     * <p>Unlike other child operations, {@code otherwise} is emitted multiple times in the bytecode
     * (once for each regular and early control flow exit). To facilitate this, the {@code
     * otherwise} operation is specified by an {@code otherwiseGenerator} that can be invoked
     * multiple times. It should be repeatable and not have side effects.
     *
     * <p>This operation is effectively a TryFinally operation with a specialized handler for the
     * exception case. It does <strong>not</strong> implement try-catch-finally semantics: if an
     * exception is thrown {@code catch} executes and {@code otherwise} does not. In pseudocode, it
     * implements:
     *
     * <pre>
     * try {
     *     tryOperation
     * } finally {
     *     if (exceptionThrown) {
     *         catchOperation
     *     } else {
     *         otherwiseOperation
     *     }
     * }
     * </pre>
     *
     * <p>This is a void operation; any of {@code try}, {@code catch}, or {@code otherwise} can be
     * void.
     *
     * <p>A corresponding call to {@link #endTryCatchOtherwise} is required to end the operation.
     *
     * @param otherwiseGenerator an idempotent Runnable that generates the {@code otherwise}
     *     operation using builder calls.
     */
    public void beginTryCatchOtherwise(Runnable otherwiseGenerator) {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.TRYCATCHOTHERWISE);
      // Initialize operation TryCatchOtherwise
      operation.setHandlerId(++state.numHandlers);
      operation.setStackHeight(state.currentStackHeight);
      operation.setFinallyGenerator(otherwiseGenerator);
      operation.setTryStartBci(state.bci);
      operation.setOperationReachable(state.reachable);
      operation.setTryReachable(state.reachable);
      operation.setCatchReachable(state.reachable);
      operation.setEndBranchFixupBci(UNINITIALIZED);
      operation.setExtraTableEntriesStart(UNINITIALIZED);
      operation.setExtraTableEntriesEnd(UNINITIALIZED);
      operation.setFinallyHandlerSp(UNINITIALIZED);
    }

    /**
     * Ends a built-in TryCatchOtherwise operation.
     *
     * <p>Signature: TryCatchOtherwise(try, catch) -> void
     *
     * @see #beginTryCatchOtherwise
     */
    public void endTryCatchOtherwise() {
      OperationStackElement operation = endOperation(Operations.TRYCATCHOTHERWISE);
      if (operation.childCount != 2) {
        throw failState(
            "Operation TryCatchOtherwise expected exactly 2 children, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      markReachable(operation.getTryReachable() || operation.getCatchReachable());
      afterChild(Operations.TRYCATCHOTHERWISE, false);
    }

    /**
     * Begins a built-in FinallyHandler operation.
     *
     * <p>Signature: FinallyHandler(body...) -> void
     *
     * <p>FinallyHandler is an internal operation that has no stack effect. All finally generators
     * execute within a FinallyHandler operation. Executing the generator emits new operations, but
     * these operations should not affect the outer operation's child count/value validation. To
     * accomplish this, FinallyHandler "hides" these operations by popping any produced values and
     * omitting calls to beforeChild/afterChild. When walking the operation stack, we skip over
     * operations above finallyOperationSp since they do not logically enclose the handler.
     *
     * <p>A corresponding call to {@link #endFinallyHandler} is required to end the operation.
     *
     * @param finallyOperationSp the operation stack pointer for the finally operation that created
     *     the FinallyHandler.
     */
    private void beginFinallyHandler(short finallyOperationSp) {
      validateRootOperationBegin();
      OperationStackElement operation = beginOperation(Operations.FINALLYHANDLER);
      // Initialize operation FinallyHandler
      operation.setFinallyOperationSp(finallyOperationSp);
    }

    /**
     * Ends a built-in FinallyHandler operation.
     *
     * <p>Signature: FinallyHandler(body...) -> void
     *
     * @see #beginFinallyHandler
     */
    private void endFinallyHandler() {
      endOperation(Operations.FINALLYHANDLER);
    }

    /**
     * Emits a built-in Label operation.
     *
     * <p>Signature: Label() -> void
     *
     * <p>Label assigns {@code label} the current location in the bytecode (so that it can be used
     * as the target of a Branch). This is a void operation.
     *
     * <p>Each {@link BytecodeLabel} must be defined exactly once. It should be defined directly
     * inside the same operation in which it is created (using {@link #createLabel}).
     *
     * @param label the label to define.
     */
    public void emitLabel(BytecodeLabel label) {
      validateRootOperationBegin();
      beforeChild();
      BytecodeLabelImpl labelImpl = (BytecodeLabelImpl) label;
      if (labelImpl.isDefined()) {
        throw failState("BytecodeLabel already emitted. Each label must be emitted exactly once.");
      }
      if (labelImpl.declaringOp != state.peekOperation().sequenceNumber) {
        throw failState(
            "BytecodeLabel must be emitted inside the same operation it was created in.");
      }
      OperationStackElement operation = state.peekOperation();
      if (operation.operation == Operations.BLOCK) {
        assert state.currentStackHeight == operation.getStartStackHeight();
      } else {
        assert operation.operation == Operations.ROOT;
        assert state.currentStackHeight == 0;
      }
      state.resolveUnresolvedLabel(labelImpl, state.currentStackHeight);
      markReachable(true);
      afterChild(Operations.LABEL, false);
    }

    /**
     * Emits a built-in Branch operation.
     *
     * <p>Signature: Branch() -> void
     *
     * <p>Branch performs a branch to {@code label}. This operation only supports unconditional
     * forward branches; use IfThen and While to perform other kinds of branches.
     *
     * @param label the label to branch to.
     */
    public void emitBranch(BytecodeLabel label) {
      validateRootOperationBegin();
      beforeChild();
      BytecodeLabelImpl labelImpl = (BytecodeLabelImpl) label;
      int declaringOperationSp = UNINITIALIZED;
      for (int i = state.operationSp - 1; i >= state.rootOperationSp; i--) {
        OperationStackElement operation = state.operationStack[i];
        if (operation.operation == Operations.FINALLYHANDLER) {
          i = operation.getFinallyOperationSp();
          continue;
        }
        if (operation.sequenceNumber == labelImpl.declaringOp) {
          declaringOperationSp = i;
          break;
        }
      }
      if (declaringOperationSp == UNINITIALIZED) {
        throw failState(
            "Branch must be targeting a label that is declared in an enclosing operation of the current root. Jumps into other operations are not permitted.");
      }
      if (labelImpl.isDefined()) {
        throw failState(
            "Backward branches are unsupported. Use a While operation to model backward control flow.");
      }
      int targetStackHeight;
      OperationStackElement operation = state.operationStack[declaringOperationSp];
      if (operation.operation == Operations.BLOCK) {
        targetStackHeight = operation.getStartStackHeight();
      } else {
        assert operation.operation == Operations.ROOT;
        targetStackHeight = 0;
      }
      beforeEmitBranch(declaringOperationSp);
      // Pop any extra values off the stack before branching.
      int stackHeightBeforeBranch = state.currentStackHeight;
      while (targetStackHeight != state.currentStackHeight) {
        state.doEmitInstruction(Instructions.POP, -1);
      }
      // If the branch is not taken (e.g., control branches over it) the values are still on the
      // stack.
      state.currentStackHeight = stackHeightBeforeBranch;
      if (state.reachable) {
        state.registerUnresolvedLabel(labelImpl, state.bci + 2);
      }
      state.doEmitInstructionI(Instructions.BRANCH, 0, UNINITIALIZED);
      markReachable(false);
      afterChild(Operations.BRANCH, false);
    }

    /**
     * Emits a built-in LoadConstant operation.
     *
     * <p>Signature: LoadConstant() -> Object
     *
     * <p>LoadConstant produces {@code constant}. The constant should be immutable, since it may be
     * shared across multiple LoadConstant operations.
     *
     * @param constant the constant value to load.
     */
    public void emitLoadConstant(Object constant) {
      validateRootOperationBegin();
      if (constant == null) {
        throw failArgument(
            "The constant parameter must not be null. Use emitLoadNull() instead for null values.");
      }
      if (constant instanceof Node && !(constant instanceof RootNode)) {
        throw failArgument("Nodes cannot be used as constants.");
      }
      beforeChild();
      state.doEmitInstructionI(Instructions.LOAD_CONSTANT, 1, state.addConstant(constant));
      afterChild(Operations.LOADCONSTANT, true);
    }

    /**
     * Emits a built-in LoadNull operation.
     *
     * <p>Signature: LoadNull() -> Object
     *
     * <p>LoadNull produces a {@code null} value.
     */
    public void emitLoadNull() {
      validateRootOperationBegin();
      beforeChild();
      state.doEmitInstruction(Instructions.LOAD_NULL, 1);
      afterChild(Operations.LOADNULL, true);
    }

    /**
     * Emits a built-in LoadArgument operation.
     *
     * <p>Signature: LoadArgument() -> Object
     *
     * <p>LoadArgument reads the argument at {@code index} from the frame. Throws {@link
     * IndexOutOfBoundsException} if the index is out of bounds.
     *
     * @param index the index of the argument to load (must fit into a short).
     */
    public void emitLoadArgument(int index) {
      validateRootOperationBegin();
      beforeChild();
      state.doEmitInstructionS(Instructions.LOAD_ARGUMENT, 1, safeCastShort(index));
      afterChild(Operations.LOADARGUMENT, true);
    }

    /**
     * Emits a built-in LoadException operation.
     *
     * <p>Signature: LoadException() -> Object
     *
     * <p>LoadException reads the current exception from the frame. This operation is only permitted
     * inside the {@code catch} operation of TryCatch and TryCatchOtherwise operations.
     */
    public void emitLoadException() {
      validateRootOperationBegin();
      beforeChild();
      int exceptionStackHeight = UNINITIALIZED;
      loop:
      for (int i = state.operationSp - 1; i >= state.rootOperationSp; i--) {
        OperationStackElement operation = state.operationStack[i];
        if (operation.operation == Operations.FINALLYHANDLER) {
          i = operation.getFinallyOperationSp();
          continue;
        }
        switch (operation.operation) {
          case Operations.TRYCATCH:
            if (operation.childCount == 1) {
              exceptionStackHeight = operation.getStackHeight();
              break loop;
            }
            break;
          case Operations.TRYCATCHOTHERWISE:
            if (operation.childCount == 1) {
              exceptionStackHeight = operation.getStackHeight();
              break loop;
            }
            break;
        }
      }
      if (exceptionStackHeight == UNINITIALIZED) {
        throw failState(
            "LoadException can only be used in the catch operation of a TryCatch/TryCatchOtherwise operation in the current root.");
      }
      state.doEmitInstructionS(Instructions.LOAD_EXCEPTION, 1, safeCastShort(exceptionStackHeight));
      afterChild(Operations.LOADEXCEPTION, true);
    }

    private void validateLocalScope(BytecodeLocal local) {
      BytecodeLocalImpl localImpl = (BytecodeLocalImpl) local;
      if (localImpl.scope.sequenceNumber != localImpl.scopeSequenceNumber
          || !localImpl.scope.getValid()) {
        throw failArgument("Local variable scope of this local is no longer valid.");
      }
      OperationStackElement rootOperation = getCurrentRootOperationData();
      if (rootOperation.getIndex() != localImpl.rootIndex) {
        throw failArgument(
            "Local variable must belong to the current root node. Consider using materialized local accesses (i.e., LoadLocalMaterialized/StoreLocalMaterialized or MaterializedLocalAccessor) to access locals from an outer root node. Materialized local accesses are currently disabled and can be enabled using the enableMaterializedLocalAccesses field of @GenerateBytecode.");
      }
    }

    /**
     * Emits a built-in LoadLocal operation.
     *
     * <p>Signature: LoadLocal() -> Object
     *
     * <p>LoadLocal reads {@code local} from the current frame. If a value has not been written to
     * the local, LoadLocal throws a {@link com.oracle.truffle.api.frame.FrameSlotTypeException}.
     *
     * @param local the local to load.
     */
    public void emitLoadLocal(BytecodeLocal local) {
      validateRootOperationBegin();
      beforeChild();
      validateLocalScope(local);
      state.doEmitInstructionS(Instructions.LOAD_LOCAL, 1, ((BytecodeLocalImpl) local).frameIndex);
      afterChild(Operations.LOADLOCAL, true);
    }

    /**
     * Begins a built-in StoreLocal operation.
     *
     * <p>Signature: StoreLocal(value) -> void
     *
     * <p>StoreLocal writes the value produced by {@code value} into the {@code local} in the
     * current frame.
     *
     * <p>A corresponding call to {@link #endStoreLocal} is required to end the operation.
     *
     * @param local the local to store to.
     */
    public void beginStoreLocal(BytecodeLocal local) {
      validateRootOperationBegin();
      validateLocalScope(local);
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.STORELOCAL);
      // Initialize operation StoreLocal
      operation.setLocal((BytecodeLocalImpl) local);
    }

    /**
     * Ends a built-in StoreLocal operation.
     *
     * <p>Signature: StoreLocal(value) -> void
     *
     * @see #beginStoreLocal
     */
    public void endStoreLocal() {
      OperationStackElement operation = endOperation(Operations.STORELOCAL);
      if (operation.childCount != 1) {
        throw failState(
            "Operation StoreLocal expected exactly 1 child, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      state.doEmitInstructionS(Instructions.STORE_LOCAL, -1, operation.getLocal().frameIndex);
      afterChild(Operations.STORELOCAL, false);
    }

    /**
     * Begins a built-in Return operation.
     *
     * <p>Signature: Return(result) -> void
     *
     * <p>Return returns the value produced by {@code result}.
     *
     * <p>A corresponding call to {@link #endReturn} is required to end the operation.
     */
    public void beginReturn() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.RETURN);
      // Initialize operation Return
      operation.setProducedValue(false);
    }

    /**
     * Ends a built-in Return operation.
     *
     * <p>Signature: Return(result) -> void
     *
     * @see #beginReturn
     */
    public void endReturn() {
      OperationStackElement operation = endOperation(Operations.RETURN);
      if (operation.childCount != 1) {
        throw failState(
            "Operation Return expected exactly 1 child, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      beforeEmitReturn(-1);
      state.doEmitInstruction(Instructions.RETURN, -1);
      markReachable(false);
      afterChild(Operations.RETURN, false);
    }

    /**
     * Begins a built-in Source operation.
     *
     * <p>Signature: Source(body...) -> void/Object
     *
     * <p>Source associates the children in its {@code body} with {@code source}. Together with
     * SourceSection, it encodes source locations for operations in the program.
     *
     * <p>A corresponding call to {@link #endSource} is required to end the operation.
     *
     * @param source the source object to associate with the enclosed operations.
     */
    public void beginSource(Source source) {
      if (!parseSources) {
        return;
      }
      beforeChild();
      if (source.hasBytes()) {
        throw failArgument("Byte-based sources are not supported.");
      }
      int index = sources.indexOf(source);
      if (index == -1) {
        index = sources.size();
        sources.add(source);
      }
      OperationStackElement operation = beginOperation(Operations.SOURCE);
      // Initialize operation Source
      operation.setSourceIndex(index);
      operation.setProducedValue(false);
    }

    /**
     * Ends a built-in Source operation.
     *
     * <p>Signature: Source(body...) -> void/Object
     *
     * @see #beginSource
     */
    public void endSource() {
      if (!parseSources) {
        return;
      }
      OperationStackElement operation = endOperation(Operations.SOURCE);
      afterChild(Operations.SOURCE, operation.getProducedValue());
    }

    /**
     * Begins a built-in SourceSectionPrefix operation.
     *
     * <p>Signature: SourceSectionPrefix(body...) -> void/Object
     *
     * <p>SourceSection associates the children in its {@code body} with the source section with the
     * given character {@code index} and {@code length}. To specify an {@link
     * Source#createUnavailableSection() unavailable source section}, provide {@code -1} for both
     * arguments. This operation must be (directly or indirectly) enclosed within a Source
     * operation.
     *
     * <p>A corresponding call to {@link #endSourceSectionPrefix} is required to end the operation.
     *
     * @param index the starting character index of the source section, or -1 if the section is
     *     unavailable.
     * @param length the length (in characters) of the source section, or -1 if the section is
     *     unavailable.
     */
    public void beginSourceSection(int index, int length) {
      if (!parseSources) {
        return;
      }
      beforeChild();
      int foundSourceIndex = -1;
      RootStackElement currentState = this.state;
      while (currentState != null) {
        loop:
        for (int i = currentState.operationSp - 1; i >= 0; i--) {
          OperationStackElement operation = currentState.operationStack[i];
          if (operation.operation == Operations.FINALLYHANDLER) {
            i = operation.getFinallyOperationSp();
            continue;
          }
          switch (currentState.operationStack[i].operation) {
            case Operations.SOURCE:
              OperationStackElement sourceData = currentState.operationStack[i];
              assert sourceData.operation == Operations.SOURCE;
              foundSourceIndex = sourceData.getSourceIndex();
              break loop;
          }
        }
        currentState = currentState.parent;
      }
      if (foundSourceIndex == -1) {
        throw failState(
            "No enclosing Source operation found - each SourceSection must be enclosed in a Source operation.");
      }
      int startBci;
      if (state.rootOperationSp == -1) {
        // not in a root yet
        startBci = 0;
      } else {
        startBci = state.bci;
      }
      if (index != -1 && length != -1) {
        if (index < 0) {
          throw new IllegalArgumentException("Invalid index provided:" + index);
        }
        if (length < 0) {
          throw new IllegalArgumentException("Invalid length provided:" + index);
        }
      }
      OperationStackElement operation = beginOperation(Operations.SOURCESECTIONPREFIX);
      // Initialize operation SourceSectionPrefix
      operation.setSourceIndex(foundSourceIndex);
      operation.setStartBci(startBci);
      operation.setStart(index);
      operation.setLength(length);
      operation.setSourceNodeId(-1);
      operation.setProducedValue(false);
    }

    /**
     * Ends a built-in SourceSectionPrefix operation.
     *
     * <p>Signature: SourceSectionPrefix(body...) -> void/Object
     *
     * @see #beginSourceSectionPrefix
     */
    public void endSourceSection() {
      if (!parseSources) {
        return;
      }
      OperationStackElement operation = endOperation(Operations.SOURCESECTIONPREFIX);
      state.doEmitSourceInfo(
          operation.getSourceIndex(),
          operation.getStartBci(),
          state.bci,
          operation.getStart(),
          operation.getLength());
      afterChild(Operations.SOURCESECTIONPREFIX, operation.getProducedValue());
    }

    /**
     * Begins a built-in SourceSectionSuffix operation.
     *
     * <p>Signature: SourceSectionSuffix(body...) -> void/Object
     *
     * <p>SourceSection associates the children in its {@code body} with the source section with the
     * given character {@code index} and {@code length}. To specify an {@link
     * Source#createUnavailableSection() unavailable source section}, provide {@code -1} for both
     * arguments. This operation must be (directly or indirectly) enclosed within a Source
     * operation.
     *
     * <p>A corresponding call to {@link #endSourceSectionSuffix} is required to end the operation.
     */
    public void beginSourceSection() {
      if (!parseSources) {
        return;
      }
      beforeChild();
      int foundSourceIndex = -1;
      RootStackElement currentState = this.state;
      while (currentState != null) {
        loop:
        for (int i = currentState.operationSp - 1; i >= 0; i--) {
          OperationStackElement operation = currentState.operationStack[i];
          if (operation.operation == Operations.FINALLYHANDLER) {
            i = operation.getFinallyOperationSp();
            continue;
          }
          switch (currentState.operationStack[i].operation) {
            case Operations.SOURCE:
              OperationStackElement sourceData = currentState.operationStack[i];
              assert sourceData.operation == Operations.SOURCE;
              foundSourceIndex = sourceData.getSourceIndex();
              break loop;
          }
        }
        currentState = currentState.parent;
      }
      if (foundSourceIndex == -1) {
        throw failState(
            "No enclosing Source operation found - each SourceSection must be enclosed in a Source operation.");
      }
      int startBci;
      if (state.rootOperationSp == -1) {
        // not in a root yet
        startBci = 0;
      } else {
        startBci = state.bci;
      }
      OperationStackElement operation = beginOperation(Operations.SOURCESECTIONSUFFIX);
      // Initialize operation SourceSectionSuffix
      operation.setSourceIndex(foundSourceIndex);
      operation.setStartBci(startBci);
      operation.setStart(-2);
      operation.setLength(-2);
      operation.setSourceNodeId(-1);
      operation.setProducedValue(false);
    }

    /**
     * Ends a built-in SourceSectionSuffix operation.
     *
     * <p>Signature: SourceSectionSuffix(body...) -> void/Object
     *
     * @param index the starting character index of the source section, or -1 if the section is
     *     unavailable.
     * @param length the length (in characters) of the source section, or -1 if the section is
     *     unavailable.
     * @see #beginSourceSectionSuffix
     */
    public void endSourceSection(int index, int length) {
      if (!parseSources) {
        return;
      }
      OperationStackElement operation = endOperation(Operations.SOURCESECTIONSUFFIX);
      if (index != -1 && length != -1) {
        if (index < 0) {
          throw new IllegalArgumentException("Invalid index provided:" + index);
        }
        if (length < 0) {
          throw new IllegalArgumentException("Invalid length provided:" + index);
        }
      }
      state.doPatchSourceInfo(
          this.builtNodes, operation.getSourceNodeId(), operation.getStart(), index, length);
      state.doEmitSourceInfo(
          operation.getSourceIndex(), operation.getStartBci(), state.bci, index, length);
      afterChild(Operations.SOURCESECTIONSUFFIX, operation.getProducedValue());
    }

    /**
     * Begins a custom {@link website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.Add
     * Add} operation.
     *
     * <p>Signature: Add(a, b) -> int
     *
     * <p>A corresponding call to {@link #endAdd} is required to end the operation.
     */
    public void beginAdd() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.ADD);
    }

    /**
     * Ends a custom {@link website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.Add
     * Add} operation.
     *
     * <p>Signature: Add(a, b) -> int
     *
     * @see #beginAdd
     */
    public void endAdd() {
      OperationStackElement operation = endOperation(Operations.ADD);
      if (operation.childCount != 2) {
        throw failState(
            "Operation Add expected exactly 2 children, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      state.doEmitInstructionI(Instructions.ADD_, -1, state.allocateNode());
      afterChild(Operations.ADD, true);
    }

    /**
     * Begins a custom {@link website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.Div
     * Div} operation.
     *
     * <p>Signature: Div(a, b) -> int
     *
     * <p>A corresponding call to {@link #endDiv} is required to end the operation.
     */
    public void beginDiv() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.DIV);
    }

    /**
     * Ends a custom {@link website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.Div
     * Div} operation.
     *
     * <p>Signature: Div(a, b) -> int
     *
     * @see #beginDiv
     */
    public void endDiv() {
      OperationStackElement operation = endOperation(Operations.DIV);
      if (operation.childCount != 2) {
        throw failState(
            "Operation Div expected exactly 2 children, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      state.doEmitInstructionI(Instructions.DIV_, -1, state.allocateNode());
      afterChild(Operations.DIV, true);
    }

    /**
     * Begins a custom {@link
     * website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.Equals Equals} operation.
     *
     * <p>Signature: Equals(a, b) -> boolean
     *
     * <p>A corresponding call to {@link #endEquals} is required to end the operation.
     */
    public void beginEquals() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.EQUALS);
    }

    /**
     * Ends a custom {@link website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.Equals
     * Equals} operation.
     *
     * <p>Signature: Equals(a, b) -> boolean
     *
     * @see #beginEquals
     */
    public void endEquals() {
      OperationStackElement operation = endOperation(Operations.EQUALS);
      if (operation.childCount != 2) {
        throw failState(
            "Operation Equals expected exactly 2 children, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      state.doEmitInstructionI(Instructions.EQUALS_, -1, state.allocateNode());
      afterChild(Operations.EQUALS, true);
    }

    /**
     * Begins a custom {@link
     * website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.LessThan LessThan}
     * operation.
     *
     * <p>Signature: LessThan(a, b) -> boolean
     *
     * <p>A corresponding call to {@link #endLessThan} is required to end the operation.
     */
    public void beginLessThan() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.LESSTHAN);
    }

    /**
     * Ends a custom {@link
     * website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.LessThan LessThan}
     * operation.
     *
     * <p>Signature: LessThan(a, b) -> boolean
     *
     * @see #beginLessThan
     */
    public void endLessThan() {
      OperationStackElement operation = endOperation(Operations.LESSTHAN);
      if (operation.childCount != 2) {
        throw failState(
            "Operation LessThan expected exactly 2 children, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      state.doEmitInstructionI(Instructions.LESS_THAN_, -1, state.allocateNode());
      afterChild(Operations.LESSTHAN, true);
    }

    /**
     * Begins a custom {@link
     * website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.EagerOr EagerOr} operation.
     *
     * <p>Signature: EagerOr(a, b) -> boolean
     *
     * <p>A corresponding call to {@link #endEagerOr} is required to end the operation.
     */
    public void beginEagerOr() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.EAGEROR);
    }

    /**
     * Ends a custom {@link website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.EagerOr
     * EagerOr} operation.
     *
     * <p>Signature: EagerOr(a, b) -> boolean
     *
     * @see #beginEagerOr
     */
    public void endEagerOr() {
      OperationStackElement operation = endOperation(Operations.EAGEROR);
      if (operation.childCount != 2) {
        throw failState(
            "Operation EagerOr expected exactly 2 children, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      state.doEmitInstructionI(Instructions.EAGER_OR_, -1, state.allocateNode());
      afterChild(Operations.EAGEROR, true);
    }

    /**
     * Begins a custom {@link
     * website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.ToBool ToBool} operation.
     *
     * <p>Signature: ToBool(b|i) -> boolean
     *
     * <p>A corresponding call to {@link #endToBool} is required to end the operation.
     */
    public void beginToBool() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.TOBOOL);
    }

    /**
     * Ends a custom {@link website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.ToBool
     * ToBool} operation.
     *
     * <p>Signature: ToBool(b|i) -> boolean
     *
     * @see #beginToBool
     */
    public void endToBool() {
      OperationStackElement operation = endOperation(Operations.TOBOOL);
      if (operation.childCount != 1) {
        throw failState(
            "Operation ToBool expected exactly 1 child, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      state.doEmitInstructionI(Instructions.TO_BOOL_, 0, state.allocateNode());
      afterChild(Operations.TOBOOL, true);
    }

    /**
     * Begins a custom {@link
     * website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.ArrayLength ArrayLength}
     * operation.
     *
     * <p>Signature: ArrayLength(array) -> int
     *
     * <p>A corresponding call to {@link #endArrayLength} is required to end the operation.
     */
    public void beginArrayLength() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.ARRAYLENGTH);
    }

    /**
     * Ends a custom {@link
     * website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.ArrayLength ArrayLength}
     * operation.
     *
     * <p>Signature: ArrayLength(array) -> int
     *
     * @see #beginArrayLength
     */
    public void endArrayLength() {
      OperationStackElement operation = endOperation(Operations.ARRAYLENGTH);
      if (operation.childCount != 1) {
        throw failState(
            "Operation ArrayLength expected exactly 1 child, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      state.doEmitInstructionI(Instructions.ARRAY_LENGTH_, 0, state.allocateNode());
      afterChild(Operations.ARRAYLENGTH, true);
    }

    /**
     * Begins a custom {@link
     * website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.ArrayIndex ArrayIndex}
     * operation.
     *
     * <p>Signature: ArrayIndex(array, index) -> int
     *
     * <p>A corresponding call to {@link #endArrayIndex} is required to end the operation.
     */
    public void beginArrayIndex() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.ARRAYINDEX);
    }

    /**
     * Ends a custom {@link
     * website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode.ArrayIndex ArrayIndex}
     * operation.
     *
     * <p>Signature: ArrayIndex(array, index) -> int
     *
     * @see #beginArrayIndex
     */
    public void endArrayIndex() {
      OperationStackElement operation = endOperation(Operations.ARRAYINDEX);
      if (operation.childCount != 2) {
        throw failState(
            "Operation ArrayIndex expected exactly 2 children, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      state.doEmitInstructionI(Instructions.ARRAY_INDEX_, -1, state.allocateNode());
      afterChild(Operations.ARRAYINDEX, true);
    }

    /**
     * Begins a custom ScOr operation.
     *
     * <p>Signature: ScOr(value) -> boolean
     *
     * <p>A corresponding call to {@link #endScOr} is required to end the operation.
     */
    public void beginScOr() {
      validateRootOperationBegin();
      beforeChild();
      OperationStackElement operation = beginOperation(Operations.SCOR);
      // Initialize operation ScOr
      operation.setNumBranchFixupBcis(0);
    }

    /**
     * Ends a custom ScOr operation.
     *
     * <p>Signature: ScOr(value) -> boolean
     *
     * @see #beginScOr
     */
    public void endScOr() {
      OperationStackElement operation = endOperation(Operations.SCOR);
      if (operation.childCount == 0) {
        throw failState(
            "Operation ScOr expected at least 1 child, but "
                + operation.childCount
                + " provided. This is probably a bug in the parser.");
      }
      state.doEmitInstructionI(Instructions.TO_BOOL_, 0, state.allocateNode());
      int[] branchFixupBcis = operation.getBranchFixupBcis();
      int numBranchFixupBcis = operation.getNumBranchFixupBcis();
      for (int i = 0; i < numBranchFixupBcis; i++) {
        BYTES.putInt(state.bc, branchFixupBcis[i], state.bci);
      }
      int nextBci;
      if (operation.childCount <= 1) {
        // Single child -> boxing elimination possible
        nextBci = state.bci - 6;
      } else {
        // Multi child -> boxing elimination not possible use short-circuit bci to disable it.
        nextBci = -1;
      }
      afterChild(Operations.SCOR, true);
    }

    private void markReachable(boolean newReachable) {
      state.reachable = newReachable;
      try {
        for (int i = state.operationSp - 1; i >= state.rootOperationSp; i--) {
          OperationStackElement operation = state.operationStack[i];
          if (operation.operation == Operations.FINALLYHANDLER) {
            i = operation.getFinallyOperationSp();
            continue;
          }
          switch (operation.operation) {
            case Operations.ROOT:
              operation.setReachable(newReachable);
              return;
            case Operations.IFTHEN:
              if (operation.childCount == 0) {
                // Unreachable condition branch makes the if and parent block unreachable.
                operation.setThenReachable(newReachable);
                continue;
              } else if (operation.childCount == 1) {
                operation.setThenReachable(newReachable);
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return;
            case Operations.IFTHENELSE:
              if (operation.childCount == 0) {
                // Unreachable condition branch makes the if, then and parent block unreachable.
                operation.setThenReachable(newReachable);
                operation.setElseReachable(newReachable);
                continue;
              } else if (operation.childCount == 1) {
                operation.setThenReachable(newReachable);
              } else if (operation.childCount == 2) {
                operation.setElseReachable(newReachable);
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return;
            case Operations.CONDITIONAL:
              if (operation.childCount == 0) {
                // Unreachable condition branch makes the if, then and parent block unreachable.
                operation.setThenReachable(newReachable);
                operation.setElseReachable(newReachable);
                continue;
              } else if (operation.childCount == 1) {
                operation.setThenReachable(newReachable);
              } else if (operation.childCount == 2) {
                operation.setElseReachable(newReachable);
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return;
            case Operations.WHILE:
              if (operation.childCount == 0) {
                operation.setBodyReachable(newReachable);
                continue;
              } else if (operation.childCount == 1) {
                operation.setBodyReachable(newReachable);
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return;
            case Operations.TRYCATCH:
              if (operation.childCount == 0) {
                operation.setTryReachable(newReachable);
              } else if (operation.childCount == 1) {
                operation.setCatchReachable(newReachable);
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return;
            case Operations.TRYFINALLY:
              if (operation.childCount == 0) {
                operation.setTryReachable(newReachable);
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return;
            case Operations.TRYCATCHOTHERWISE:
              if (operation.childCount == 0) {
                operation.setTryReachable(newReachable);
              } else if (operation.childCount == 1) {
                operation.setCatchReachable(newReachable);
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return;
          }
        }
      } finally {
        assert updateReachable() == state.reachable : "Inconsistent reachability detected.";
      }
    }

    /**
     * Updates the reachable field from the current operation. Typically invoked when the operation
     * ended or the child is changing.
     */
    private boolean updateReachable() {
      boolean oldReachable = state.reachable;
      for (int i = state.operationSp - 1; i >= state.rootOperationSp; i--) {
        OperationStackElement operation = state.operationStack[i];
        if (operation.operation == Operations.FINALLYHANDLER) {
          i = operation.getFinallyOperationSp();
          continue;
        }
        switch (operation.operation) {
          case Operations.ROOT:
            {
              state.reachable = operation.getReachable();
              return oldReachable;
            }
          case Operations.IFTHEN:
            {
              if (operation.childCount == 0) {
                continue;
              } else if (operation.childCount == 1) {
                state.reachable = operation.getThenReachable();
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return oldReachable;
            }
          case Operations.IFTHENELSE:
            {
              if (operation.childCount == 0) {
                // Unreachable condition branch makes the if, then and parent block unreachable.
                continue;
              } else if (operation.childCount == 1) {
                state.reachable = operation.getThenReachable();
              } else if (operation.childCount == 2) {
                state.reachable = operation.getElseReachable();
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return oldReachable;
            }
          case Operations.CONDITIONAL:
            {
              if (operation.childCount == 0) {
                // Unreachable condition branch makes the if, then and parent block unreachable.
                continue;
              } else if (operation.childCount == 1) {
                state.reachable = operation.getThenReachable();
              } else if (operation.childCount == 2) {
                state.reachable = operation.getElseReachable();
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return oldReachable;
            }
          case Operations.WHILE:
            {
              if (operation.childCount == 0) {
                continue;
              } else if (operation.childCount == 1) {
                state.reachable = operation.getBodyReachable();
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return oldReachable;
            }
          case Operations.TRYCATCH:
            {
              if (operation.childCount == 0) {
                state.reachable = operation.getTryReachable();
              } else if (operation.childCount == 1) {
                state.reachable = operation.getCatchReachable();
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return oldReachable;
            }
          case Operations.TRYFINALLY:
            {
              if (operation.childCount == 0) {
                state.reachable = operation.getTryReachable();
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return oldReachable;
            }
          case Operations.TRYCATCHOTHERWISE:
            {
              if (operation.childCount == 0) {
                state.reachable = operation.getTryReachable();
              } else if (operation.childCount == 2) {
                state.reachable = operation.getCatchReachable();
              } else {
                // Invalid child index, but we will fail in the end method.
              }
              return oldReachable;
            }
        }
      }
      return oldReachable;
    }

    private OperationStackElement beginOperation(int id) {
      return state.pushOperation(id);
    }

    private OperationStackElement endOperation(int id) {
      verifyOperation(id);
      return state.popOperation();
    }

    private OperationStackElement verifyOperation(int id) {
      OperationStackElement entry = state.peekOperation();
      if (entry == null) {
        throw failState(
            "Unexpected operation end - there are no operations on the stack. Did you forget a beginRoot()?");
      } else if (entry.operation != id) {
        throw failState(
            "Unexpected operation end, expected end%s, but got end%s.",
            Operations.getName(entry.operation), Operations.getName(id));
      }
      return entry;
    }

    private void validateRootOperationBegin() {
      if (state.rootOperationSp == -1) {
        throw failState(
            "Unexpected operation emit - no root operation present. Did you forget a beginRoot()?");
      }
    }

    private OperationStackElement getCurrentRootOperationData() {
      validateRootOperationBegin();
      return state.operationStack[state.rootOperationSp];
    }

    private void beforeChild() {
      if (state.operationSp == 0) {
        return;
      }
      OperationStackElement operation = state.peekOperation();
      int childIndex = operation.childCount;
      switch (operation.operation) {
        case Operations.BLOCK:
        case Operations.ROOT:
        case Operations.SOURCE:
        case Operations.SOURCESECTIONPREFIX:
        case Operations.SOURCESECTIONSUFFIX:
          {
            if (operation.getProducedValue()) {
              state.doEmitInstruction(Instructions.POP, -1);
            }
            break;
          }
        case Operations.IFTHEN:
        case Operations.IFTHENELSE:
        case Operations.CONDITIONAL:
        case Operations.TRYFINALLY:
          {
            if (childIndex >= 1) {
              updateReachable();
            }
            break;
          }
        case Operations.WHILE:
        case Operations.FINALLYHANDLER:
        case Operations.STORELOCAL:
        case Operations.RETURN:
        case Operations.ADD:
        case Operations.DIV:
        case Operations.EQUALS:
        case Operations.LESSTHAN:
        case Operations.EAGEROR:
        case Operations.TOBOOL:
        case Operations.ARRAYLENGTH:
        case Operations.ARRAYINDEX:
          {
            break;
          }
        case Operations.TRYCATCH:
        case Operations.TRYCATCHOTHERWISE:
          {
            if (childIndex == 1) {
              updateReachable();
              // The exception dispatch logic pushes the exception onto the stack.
              state.currentStackHeight = state.currentStackHeight + 1;
              state.updateMaxStackHeight(state.currentStackHeight);
            }
            break;
          }
        case Operations.SCOR:
          {
            if (childIndex != 0) {
              state.doEmitInstructionI(Instructions.TO_BOOL_, 0, state.allocateNode());
              if (state.reachable) {
                int[] branchFixupBcis = operation.getBranchFixupBcis();
                int numBranchFixupBcis = operation.getNumBranchFixupBcis();
                if (numBranchFixupBcis >= branchFixupBcis.length) {
                  branchFixupBcis = Arrays.copyOf(branchFixupBcis, branchFixupBcis.length * 2);
                  operation.setBranchFixupBcis(branchFixupBcis);
                }
                branchFixupBcis[numBranchFixupBcis] = state.bci + 2;
                operation.setNumBranchFixupBcis(numBranchFixupBcis + 1);
              }
              state.doEmitInstructionII(
                  Instructions.SC_OR_, -1, UNINITIALIZED, state.allocateBranchProfile());
            }
            break;
          }
        default:
          throw assertionFailed(
              "beforeChild should not be called on an operation with no children.");
      }
    }

    private void afterChild(int operationCode, boolean producedValue) {
      if (state.operationSp == 0) {
        return;
      }
      OperationStackElement operation = state.peekOperation();
      int childIndex = operation.childCount;
      switch (operation.operation) {
        case Operations.BLOCK:
        case Operations.ROOT:
        case Operations.SOURCE:
        case Operations.SOURCESECTIONPREFIX:
        case Operations.SOURCESECTIONSUFFIX:
          {
            operation.setProducedValue(producedValue);
            break;
          }
        case Operations.IFTHEN:
          {
            if ((childIndex == 0) && !producedValue) {
              throw failState(
                  "Operation %s expected a value-producing child at position %s, but a void one was provided.",
                  Operations.getName(operation.operation), childIndex);
            } else if ((childIndex == 1) && producedValue) {
              state.doEmitInstruction(Instructions.POP, -1);
            }
            if (childIndex == 0) {
              if (state.reachable) {
                operation.setFalseBranchFixupBci(state.bci + 2);
              }
              state.doEmitInstructionII(
                  Instructions.BRANCH_FALSE, -1, UNINITIALIZED, state.allocateBranchProfile());
            } else {
              int toUpdate = operation.getFalseBranchFixupBci();
              if (toUpdate != UNINITIALIZED) {
                BYTES.putInt(state.bc, toUpdate, state.bci);
              }
            }
            break;
          }
        case Operations.IFTHENELSE:
          {
            if ((childIndex == 0) && !producedValue) {
              throw failState(
                  "Operation %s expected a value-producing child at position %s, but a void one was provided.",
                  Operations.getName(operation.operation), childIndex);
            } else if ((childIndex == 1 || childIndex == 2) && producedValue) {
              state.doEmitInstruction(Instructions.POP, -1);
            }
            if (childIndex == 0) {
              if (state.reachable) {
                operation.setFalseBranchFixupBci(state.bci + 2);
              }
              state.doEmitInstructionII(
                  Instructions.BRANCH_FALSE, -1, UNINITIALIZED, state.allocateBranchProfile());
            } else if (childIndex == 1) {
              if (state.reachable) {
                operation.setEndBranchFixupBci(state.bci + 2);
              }
              state.doEmitInstructionI(Instructions.BRANCH, 0, UNINITIALIZED);
              int toUpdate = operation.getFalseBranchFixupBci();
              if (toUpdate != UNINITIALIZED) {
                BYTES.putInt(state.bc, toUpdate, state.bci);
              }
            } else {
              int toUpdate = operation.getEndBranchFixupBci();
              if (toUpdate != UNINITIALIZED) {
                BYTES.putInt(state.bc, toUpdate, state.bci);
              }
            }
            break;
          }
        case Operations.CONDITIONAL:
          {
            if (!producedValue) {
              throw failState(
                  "Operation %s expected a value-producing child at position %s, but a void one was provided.",
                  Operations.getName(operation.operation), childIndex);
            }
            if (childIndex == 0) {
              if (state.reachable) {
                operation.setFalseBranchFixupBci(state.bci + 2);
              }
              state.doEmitInstructionII(
                  Instructions.BRANCH_FALSE, -1, UNINITIALIZED, state.allocateBranchProfile());
            } else if (childIndex == 1) {
              if (state.reachable) {
                operation.setEndBranchFixupBci(state.bci + 2);
                state.doEmitInstructionI(Instructions.BRANCH, 0, UNINITIALIZED);
              }
              state.currentStackHeight -= 1;
              int toUpdate = operation.getFalseBranchFixupBci();
              if (toUpdate != UNINITIALIZED) {
                BYTES.putInt(state.bc, toUpdate, state.bci);
              }
            } else {
              int toUpdate = operation.getEndBranchFixupBci();
              if (toUpdate != UNINITIALIZED) {
                BYTES.putInt(state.bc, toUpdate, state.bci);
              }
            }
            break;
          }
        case Operations.WHILE:
          {
            if ((childIndex == 0) && !producedValue) {
              throw failState(
                  "Operation %s expected a value-producing child at position %s, but a void one was provided.",
                  Operations.getName(operation.operation), childIndex);
            } else if ((childIndex == 1) && producedValue) {
              state.doEmitInstruction(Instructions.POP, -1);
            }
            if (childIndex == 0) {
              if (state.reachable) {
                operation.setEndBranchFixupBci(state.bci + 2);
              }
              state.doEmitInstructionII(
                  Instructions.BRANCH_FALSE, -1, UNINITIALIZED, state.allocateBranchProfile());
            } else {
              int toUpdate = operation.getEndBranchFixupBci();
              if (toUpdate != UNINITIALIZED) {
                state.doEmitInstructionII(
                    Instructions.BRANCH_BACKWARD,
                    0,
                    operation.getWhileStartBci(),
                    BYTES.getInt(state.bc, toUpdate + 4 /* loop branch profile */));
                BYTES.putInt(state.bc, toUpdate, state.bci);
              }
            }
            break;
          }
        case Operations.TRYCATCH:
          {
            if (producedValue) {
              state.doEmitInstruction(Instructions.POP, -1);
            }
            if (childIndex == 0) {
              if (operation.getOperationReachable()) {
                int tryEndBci = state.bci;
                if (operation.getTryReachable()) {
                  operation.setEndBranchFixupBci(state.bci + 2);
                  state.doEmitInstructionI(Instructions.BRANCH, 0, UNINITIALIZED);
                }
                int handlerSp = state.currentStackHeight + 1;
                state.patchHandlerTable(
                    operation.getExtraTableEntriesStart(),
                    operation.getExtraTableEntriesEnd(),
                    operation.getHandlerId(),
                    state.bci,
                    handlerSp);
                state.doCreateExceptionHandler(
                    operation.getTryStartBci(), tryEndBci, HANDLER_CUSTOM, state.bci, handlerSp);
              }
            } else if (childIndex == 1) {
              // pop the exception
              state.doEmitInstruction(Instructions.POP, -1);
              if (operation.getEndBranchFixupBci() != UNINITIALIZED) {
                BYTES.putInt(state.bc, operation.getEndBranchFixupBci(), state.bci);
              }
            }
            break;
          }
        case Operations.TRYFINALLY:
        case Operations.FINALLYHANDLER:
          {
            if (producedValue) {
              state.doEmitInstruction(Instructions.POP, -1);
            }
            break;
          }
        case Operations.TRYCATCHOTHERWISE:
          {
            if (producedValue) {
              state.doEmitInstruction(Instructions.POP, -1);
            }
            if (childIndex == 0) {
              int handlerSp = state.currentStackHeight + 1 /* reserve space for the exception */;
              state.updateMaxStackHeight(handlerSp);
              int exHandlerIndex = UNINITIALIZED;
              if (operation.getOperationReachable()) {
                // register exception table entry
                exHandlerIndex =
                    state.doCreateExceptionHandler(
                        operation.getTryStartBci(),
                        state.bci,
                        HANDLER_CUSTOM,
                        -operation.getHandlerId(),
                        handlerSp);
              }
              // emit handler for normal completion case
              doEmitFinallyHandler(operation, state.operationSp - 1);
              // the operation was popped, so manually update reachability. try is reachable if
              // neither it nor the finally handler exited early.
              operation.setTryReachable(operation.getTryReachable() && state.reachable);
              if (state.reachable) {
                operation.setEndBranchFixupBci(state.bci + 2);
                state.doEmitInstructionI(Instructions.BRANCH, 0, UNINITIALIZED);
              }
              if (operation.getOperationReachable()) {
                // update exception table; force handler code to be reachable
                state.reachable = true;
                state.patchHandlerTable(
                    operation.getExtraTableEntriesStart(),
                    operation.getExtraTableEntriesEnd(),
                    operation.getHandlerId(),
                    state.bci,
                    handlerSp);
                if (exHandlerIndex != UNINITIALIZED) {
                  state.handlerTable[exHandlerIndex + EXCEPTION_HANDLER_OFFSET_HANDLER_BCI] =
                      state.bci;
                }
              }
            } else {
              // pop the exception
              state.doEmitInstruction(Instructions.POP, -1);
              if (operation.getEndBranchFixupBci() != UNINITIALIZED) {
                BYTES.putInt(state.bc, operation.getEndBranchFixupBci(), state.bci);
              }
            }
            break;
          }
        case Operations.STORELOCAL:
        case Operations.ADD:
        case Operations.DIV:
        case Operations.EQUALS:
        case Operations.LESSTHAN:
        case Operations.EAGEROR:
        case Operations.TOBOOL:
        case Operations.ARRAYLENGTH:
        case Operations.ARRAYINDEX:
        case Operations.SCOR:
          {
            if (!producedValue) {
              throw failState(
                  "Operation %s expected a value-producing child at position %s, but a void one was provided.",
                  Operations.getName(operation.operation), childIndex);
            }
            break;
          }
        case Operations.RETURN:
          {
            if (!producedValue) {
              throw failState(
                  "Operation %s expected a value-producing child at position %s, but a void one was provided.",
                  Operations.getName(operation.operation), childIndex);
            }
            operation.setProducedValue(producedValue);
            break;
          }
      }
      operation.childCount = childIndex + 1;
    }

    private void doEmitFinallyHandler(
        OperationStackElement tryFinallyData, int finallyOperationSp) {
      assert state.operationStack[finallyOperationSp].operation == Operations.TRYFINALLY
          || state.operationStack[finallyOperationSp].operation == Operations.TRYCATCHOTHERWISE;
      assert tryFinallyData.getFinallyHandlerSp() == UNINITIALIZED;
      try {
        tryFinallyData.setFinallyHandlerSp(state.operationSp);
        beginFinallyHandler(safeCastShort(finallyOperationSp));
        tryFinallyData.getFinallyGenerator().run();
        endFinallyHandler();
      } finally {
        tryFinallyData.setFinallyHandlerSp(UNINITIALIZED);
      }
    }

    private void finish() {
      if (state.operationSp != 0) {
        throw failState(
            "Unexpected parser end - there are still operations on the stack. Did you forget to end them?");
      }
      if (reparseReason == null) {
        nodes.setNodes(builtNodes.toArray(new GettingStartedBytecodeRootNodeGen[0]));
      }
      assert nodes.validate();
      RootStackElement.release(state);
      // make sure its no longer used
      this.state = null;
    }

    /**
     * Walks the operation stack, emitting instructions for any operations that need to complete
     * before the branch (and fixing up bytecode ranges to exclude these instructions).
     */
    private void beforeEmitBranch(int declaringOperationSp) {
      /**
       * Emit "exit" instructions for any pending operations, and close any bytecode ranges that
       * should not apply to the emitted instructions.
       */
      boolean needsRewind = false;
      for (int i = state.operationSp - 1; i >= declaringOperationSp + 1; i--) {
        OperationStackElement operation = state.operationStack[i];
        if (operation.operation == Operations.FINALLYHANDLER) {
          i = operation.getFinallyOperationSp();
          continue;
        }
        switch (operation.operation) {
          case Operations.TRYFINALLY:
            {
              if (operation.childCount == 0 /* still in try */) {
                if (state.reachable) {
                  int handlerTableIndex =
                      state.doCreateExceptionHandler(
                          operation.getTryStartBci(),
                          state.bci,
                          HANDLER_CUSTOM,
                          -operation.getHandlerId(),
                          UNINITIALIZED /* stack height */);
                  if (handlerTableIndex != UNINITIALIZED) {
                    if (operation.getExtraTableEntriesStart() == UNINITIALIZED) {
                      operation.setExtraTableEntriesStart(handlerTableIndex);
                    }
                    operation.setExtraTableEntriesEnd(handlerTableIndex + EXCEPTION_HANDLER_LENGTH);
                  }
                  needsRewind = true;
                }
                doEmitFinallyHandler(operation, i);
              }
              break;
            }
          case Operations.TRYCATCHOTHERWISE:
            {
              if (operation.childCount == 0 /* still in try */) {
                if (state.reachable) {
                  int handlerTableIndex =
                      state.doCreateExceptionHandler(
                          operation.getTryStartBci(),
                          state.bci,
                          HANDLER_CUSTOM,
                          -operation.getHandlerId(),
                          UNINITIALIZED /* stack height */);
                  if (handlerTableIndex != UNINITIALIZED) {
                    if (operation.getExtraTableEntriesStart() == UNINITIALIZED) {
                      operation.setExtraTableEntriesStart(handlerTableIndex);
                    }
                    operation.setExtraTableEntriesEnd(handlerTableIndex + EXCEPTION_HANDLER_LENGTH);
                  }
                  needsRewind = true;
                }
                doEmitFinallyHandler(operation, i);
              }
              break;
            }
          case Operations.TRYCATCH:
            {
              if (operation.childCount == 0 /* still in try */ && state.reachable) {
                int handlerTableIndex =
                    state.doCreateExceptionHandler(
                        operation.getTryStartBci(),
                        state.bci,
                        HANDLER_CUSTOM,
                        -operation.getHandlerId(),
                        UNINITIALIZED /* stack height */);
                if (handlerTableIndex != UNINITIALIZED) {
                  if (operation.getExtraTableEntriesStart() == UNINITIALIZED) {
                    operation.setExtraTableEntriesStart(handlerTableIndex);
                  }
                  operation.setExtraTableEntriesEnd(handlerTableIndex + EXCEPTION_HANDLER_LENGTH);
                }
                needsRewind = true;
              }
              break;
            }
          case Operations.SOURCESECTIONPREFIX:
            {
              state.doEmitSourceInfo(
                  operation.getSourceIndex(),
                  operation.getStartBci(),
                  state.bci,
                  operation.getStart(),
                  operation.getLength());
              needsRewind = true;
              break;
            }
          case Operations.SOURCESECTIONSUFFIX:
            {
              int operationStart =
                  state.doEmitSourceInfo(
                      operation.getSourceIndex(),
                      operation.getStartBci(),
                      state.bci,
                      operation.getStart(),
                      PATCH_CURRENT_SOURCE);
              operation.setStart(operationStart);
              needsRewind = true;
              break;
            }
          case Operations.BLOCK:
            {
              for (int j = 0; j < operation.getNumLocals(); j++) {
                state.locals[operation.getLocals()[j] + LOCALS_OFFSET_END_BCI] = state.bci;
                state.doEmitInstructionS(
                    Instructions.CLEAR_LOCAL,
                    0,
                    safeCastShort(
                        state.locals[operation.getLocals()[j] + LOCALS_OFFSET_FRAME_INDEX]));
                needsRewind = true;
              }
              break;
            }
        }
      }
      /** Now that all "exit" instructions have been emitted, reopen bytecode ranges. */
      if (needsRewind) {
        for (int i = declaringOperationSp + 1; i < state.operationSp; i++) {
          OperationStackElement operation = state.operationStack[i];
          switch (operation.operation) {
            case Operations.TRYFINALLY:
            case Operations.TRYCATCHOTHERWISE:
              int finallyHandlerSp = operation.getFinallyHandlerSp();
              if (finallyHandlerSp != UNINITIALIZED) {
                i = finallyHandlerSp - 1;
                continue;
              }
              break;
            default:
              break;
          }
          switch (operation.operation) {
            case Operations.TRYFINALLY:
            case Operations.TRYCATCHOTHERWISE:
              if (operation.childCount == 0 /* still in try */) {
                operation.setTryStartBci(state.bci);
              }
              break;
            case Operations.TRYCATCH:
              if (operation.childCount == 0 /* still in try */) {
                operation.setTryStartBci(state.bci);
              }
              break;
            case Operations.SOURCESECTIONPREFIX:
              {
                operation.setStartBci(state.bci);
                break;
              }
            case Operations.SOURCESECTIONSUFFIX:
              {
                operation.setStartBci(state.bci);
                break;
              }
            case Operations.BLOCK:
              {
                for (int j = 0; j < operation.getNumLocals(); j++) {
                  int prevTableIndex = operation.getLocals()[j];
                  // Create a new table entry with a new bytecode range and the same metadata.
                  int localIndex = state.locals[prevTableIndex + LOCALS_OFFSET_LOCAL_INDEX];
                  int frameIndex = state.locals[prevTableIndex + LOCALS_OFFSET_FRAME_INDEX];
                  int nameIndex = state.locals[prevTableIndex + LOCALS_OFFSET_NAME];
                  int infoIndex = state.locals[prevTableIndex + LOCALS_OFFSET_INFO];
                  operation.getLocals()[j] =
                      state.doEmitLocal(localIndex, frameIndex, nameIndex, infoIndex);
                }
              }
          }
        }
      }
    }

    /**
     * Walks the operation stack, emitting instructions for any operations that need to complete
     * before the return (and fixing up bytecode ranges to exclude these instructions).
     */
    private void beforeEmitReturn(int parentBci) {
      /**
       * Emit "exit" instructions for any pending operations, and close any bytecode ranges that
       * should not apply to the emitted instructions.
       */
      int childBci = parentBci;
      boolean needsRewind = false;
      for (int i = state.operationSp - 1; i >= state.rootOperationSp + 1; i--) {
        OperationStackElement operation = state.operationStack[i];
        if (operation.operation == Operations.FINALLYHANDLER) {
          i = operation.getFinallyOperationSp();
          continue;
        }
        switch (operation.operation) {
          case Operations.TRYFINALLY:
            {
              if (operation.childCount == 0 /* still in try */) {
                if (state.reachable) {
                  int handlerTableIndex =
                      state.doCreateExceptionHandler(
                          operation.getTryStartBci(),
                          state.bci,
                          HANDLER_CUSTOM,
                          -operation.getHandlerId(),
                          UNINITIALIZED /* stack height */);
                  if (handlerTableIndex != UNINITIALIZED) {
                    if (operation.getExtraTableEntriesStart() == UNINITIALIZED) {
                      operation.setExtraTableEntriesStart(handlerTableIndex);
                    }
                    operation.setExtraTableEntriesEnd(handlerTableIndex + EXCEPTION_HANDLER_LENGTH);
                  }
                  needsRewind = true;
                }
                doEmitFinallyHandler(operation, i);
              }
              break;
            }
          case Operations.TRYCATCHOTHERWISE:
            {
              if (operation.childCount == 0 /* still in try */) {
                if (state.reachable) {
                  int handlerTableIndex =
                      state.doCreateExceptionHandler(
                          operation.getTryStartBci(),
                          state.bci,
                          HANDLER_CUSTOM,
                          -operation.getHandlerId(),
                          UNINITIALIZED /* stack height */);
                  if (handlerTableIndex != UNINITIALIZED) {
                    if (operation.getExtraTableEntriesStart() == UNINITIALIZED) {
                      operation.setExtraTableEntriesStart(handlerTableIndex);
                    }
                    operation.setExtraTableEntriesEnd(handlerTableIndex + EXCEPTION_HANDLER_LENGTH);
                  }
                  needsRewind = true;
                }
                doEmitFinallyHandler(operation, i);
              }
              break;
            }
          case Operations.TRYCATCH:
            {
              if (operation.childCount == 0 /* still in try */ && state.reachable) {
                int handlerTableIndex =
                    state.doCreateExceptionHandler(
                        operation.getTryStartBci(),
                        state.bci,
                        HANDLER_CUSTOM,
                        -operation.getHandlerId(),
                        UNINITIALIZED /* stack height */);
                if (handlerTableIndex != UNINITIALIZED) {
                  if (operation.getExtraTableEntriesStart() == UNINITIALIZED) {
                    operation.setExtraTableEntriesStart(handlerTableIndex);
                  }
                  operation.setExtraTableEntriesEnd(handlerTableIndex + EXCEPTION_HANDLER_LENGTH);
                }
                needsRewind = true;
              }
              break;
            }
          case Operations.SOURCESECTIONPREFIX:
            {
              state.doEmitSourceInfo(
                  operation.getSourceIndex(),
                  operation.getStartBci(),
                  state.bci,
                  operation.getStart(),
                  operation.getLength());
              needsRewind = true;
              break;
            }
          case Operations.SOURCESECTIONSUFFIX:
            {
              int operationStart =
                  state.doEmitSourceInfo(
                      operation.getSourceIndex(),
                      operation.getStartBci(),
                      state.bci,
                      operation.getStart(),
                      PATCH_CURRENT_SOURCE);
              operation.setStart(operationStart);
              needsRewind = true;
              break;
            }
          case Operations.BLOCK:
            {
              for (int j = 0; j < operation.getNumLocals(); j++) {
                state.locals[operation.getLocals()[j] + LOCALS_OFFSET_END_BCI] = state.bci;
                needsRewind = true;
              }
              break;
            }
        }
      }
      /** Now that all "exit" instructions have been emitted, reopen bytecode ranges. */
      if (needsRewind) {
        for (int i = state.rootOperationSp + 1; i < state.operationSp; i++) {
          OperationStackElement operation = state.operationStack[i];
          switch (operation.operation) {
            case Operations.TRYFINALLY:
            case Operations.TRYCATCHOTHERWISE:
              int finallyHandlerSp = operation.getFinallyHandlerSp();
              if (finallyHandlerSp != UNINITIALIZED) {
                i = finallyHandlerSp - 1;
                continue;
              }
              break;
            default:
              break;
          }
          switch (operation.operation) {
            case Operations.TRYFINALLY:
            case Operations.TRYCATCHOTHERWISE:
              if (operation.childCount == 0 /* still in try */) {
                operation.setTryStartBci(state.bci);
              }
              break;
            case Operations.TRYCATCH:
              if (operation.childCount == 0 /* still in try */) {
                operation.setTryStartBci(state.bci);
              }
              break;
            case Operations.SOURCESECTIONPREFIX:
              {
                operation.setStartBci(state.bci);
                break;
              }
            case Operations.SOURCESECTIONSUFFIX:
              {
                operation.setStartBci(state.bci);
                break;
              }
            case Operations.BLOCK:
              {
                for (int j = 0; j < operation.getNumLocals(); j++) {
                  int prevTableIndex = operation.getLocals()[j];
                  int endBci = state.locals[prevTableIndex + LOCALS_OFFSET_END_BCI];
                  if (endBci == state.bci) {
                    // No need to split. Reuse the existing entry.
                    state.locals[prevTableIndex + LOCALS_OFFSET_END_BCI] = UNINITIALIZED;
                    continue;
                  }
                  // Create a new table entry with a new bytecode range and the same metadata.
                  int localIndex = state.locals[prevTableIndex + LOCALS_OFFSET_LOCAL_INDEX];
                  int frameIndex = state.locals[prevTableIndex + LOCALS_OFFSET_FRAME_INDEX];
                  int nameIndex = state.locals[prevTableIndex + LOCALS_OFFSET_NAME];
                  int infoIndex = state.locals[prevTableIndex + LOCALS_OFFSET_INFO];
                  operation.getLocals()[j] =
                      state.doEmitLocal(localIndex, frameIndex, nameIndex, infoIndex);
                }
              }
          }
        }
      }
    }

    private void doEmitRootSourceSection(int nodeId) {
      if (!parseSources) {
        // Nothing to do here without sources
        return;
      }
      RootStackElement currentState = state;
      while (currentState != null) {
        for (int i = currentState.operationSp - 1; i >= 0; i--) {
          OperationStackElement operation = currentState.operationStack[i];
          if (operation.operation == Operations.FINALLYHANDLER) {
            i = operation.getFinallyOperationSp();
            continue;
          }
          switch (operation.operation) {
            case Operations.SOURCESECTIONPREFIX:
              {
                state.doEmitSourceInfo(
                    operation.getSourceIndex(),
                    0,
                    state.bci,
                    operation.getStart(),
                    operation.getLength());
                return;
              }
            case Operations.SOURCESECTIONSUFFIX:
              {
                operation.setSourceNodeId(nodeId);
                int operationStart =
                    state.doEmitSourceInfo(
                        operation.getSourceIndex(),
                        0,
                        state.bci,
                        operation.getStart(),
                        PATCH_NODE_SOURCE);
                operation.setStart(operationStart);
                return;
              }
          }
        }
        currentState = currentState.parent;
      }
    }

    private OperationStackElement getCurrentScope() {
      for (int i = state.operationSp - 1; i >= state.rootOperationSp; i--) {
        OperationStackElement operation = state.operationStack[i];
        if (operation.operation == Operations.FINALLYHANDLER) {
          i = operation.getFinallyOperationSp();
          continue;
        }
        switch (operation.operation) {
          case Operations.ROOT:
          case Operations.BLOCK:
            return operation;
        }
      }
      throw failState("Invalid scope for local variable.");
    }

    @Override
    public String toString() {
      StringBuilder b = new StringBuilder();
      b.append(GettingStartedBytecodeRootNodeGen.class.getSimpleName());
      b.append('.');
      b.append(website.lihan.temu.GettingStartedBytecodeRootNodeGen.Builder.class.getSimpleName());
      b.append("[");
      b.append("mode=");
      if (reparseReason != null) {
        b.append("reparsing");
      } else {
        b.append("default");
      }
      b.append(", bytecodeIndex=").append(state.bci);
      b.append(", stackPointer=").append(state.currentStackHeight);
      b.append(", bytecodes=").append(parseBytecodes);
      b.append(", sources=").append(parseSources);
      b.append(",");
      b.append(System.lineSeparator());
      b.append("  current=");
      b.append(state.toString());
      b.append("]");
      return b.toString();
    }

    private RuntimeException failState(String message, Object... args) {
      throw new IllegalStateException(
          "Invalid builder usage: "
              + String.format(message, args)
              + " Operation stack: "
              + dumpAt());
    }

    private RuntimeException failArgument(String message) {
      throw new IllegalArgumentException(
          "Invalid builder operation argument: " + message + " Operation stack: " + dumpAt());
    }

    private String dumpAt() {
      try {
        return state.toString();
      } catch (Exception e) {
        return "<invalid-location>";
      }
    }

    private static short safeCastShort(int num) {
      if (Short.MIN_VALUE <= num && num <= Short.MAX_VALUE) {
        return (short) num;
      }
      throw BytecodeEncodingException.create("Value " + num + " cannot be encoded as a short.");
    }

    private static short checkOverflowShort(short num, String valueName) {
      if (num < 0) {
        throw BytecodeEncodingException.create(valueName + " overflowed.");
      }
      return num;
    }

    private static int checkOverflowInt(int num, String valueName) {
      if (num < 0) {
        throw BytecodeEncodingException.create(valueName + " overflowed.");
      }
      return num;
    }

    private static int checkBci(int newBci) {
      return checkOverflowInt(newBci, "Bytecode index");
    }

    private static final class RootStackElement {

      private static final ThreadLocal<SoftReference<RootStackElement>> THREAD_LOCAL =
          new ThreadLocal<>();

      private final RootStackElement parent;
      private RootStackElement next;
      private boolean needsClean;
      private SoftReference<RootStackElement> reference = new SoftReference<RootStackElement>(this);
      private int operationSequenceNumber;
      private OperationStackElement[] operationStack;
      private int operationSp;
      private int rootOperationSp;
      private boolean reachable = true;
      private byte[] bc;
      private int bci;
      private int numLocals;
      private int numLabels;
      private int numNodes;
      private int numHandlers;
      private int numConditionalBranches;
      private int currentStackHeight;
      private int maxStackHeight;
      private int[] sourceInfo;
      private int sourceInfoIndex;
      private int[] handlerTable;
      private int handlerTableSize;
      private int[] locals;
      private int localsTableIndex;
      private final ConstantsBuffer constants;
      private int maxLocals;

      RootStackElement(RootStackElement parent) {
        this.parent = parent;
        this.operationSequenceNumber = 0;
        this.rootOperationSp = -1;
        this.operationStack = new OperationStackElement[32];
        for (int i = 0; i < this.operationStack.length; i++) {
          this.operationStack[i] = new OperationStackElement();
        }
        this.reachable = true;
        this.numLocals = 0;
        this.maxLocals = numLocals;
        this.numLabels = 0;
        this.numNodes = 0;
        this.numHandlers = 0;
        this.numConditionalBranches = 0;
        this.constants = new ConstantsBuffer();
        this.bc = new byte[512];
        this.bci = 0;
        this.currentStackHeight = 0;
        this.maxStackHeight = 0;
        this.handlerTable = new int[8 * EXCEPTION_HANDLER_LENGTH];
        this.handlerTableSize = 0;
        this.locals = null;
        this.localsTableIndex = 0;
        this.sourceInfo = new int[16 * SOURCE_INFO_LENGTH];
        this.sourceInfoIndex = 0;
      }

      private RootStackElement getNext() {
        if (this.next == null) {
          this.next = new RootStackElement(this);
        }
        return this.next;
      }

      /** Cleans up all object references before releasing it to the shared cache. */
      private void cleanup() {
        if (!needsClean) {
          return;
        }
        // Only reset this for each set of root nodes.
        this.operationSequenceNumber = 0;
        // clear references to potentially expensive references
        this.constants.clear();
        if (this.next != null) {
          this.next.cleanup();
        }
        needsClean = false;
      }

      /** Resets all internal state to be usable for the next root. */
      private void reset() {
        this.rootOperationSp = -1;
        this.reachable = true;
        this.numLocals = 0;
        this.maxLocals = 0;
        this.numLabels = 0;
        this.numNodes = 0;
        this.numHandlers = 0;
        this.numConditionalBranches = 0;
        this.bci = 0;
        this.currentStackHeight = 0;
        this.maxStackHeight = 0;
        this.handlerTableSize = 0;
        this.localsTableIndex = 0;
        this.sourceInfoIndex = 0;
        this.needsClean = true;
      }

      private OperationStackElement pushOperation(int operation) {
        int sp = this.operationSp;
        OperationStackElement[] stack = this.operationStack;
        if (sp == stack.length) {
          stack = this.operationStack = Arrays.copyOf(stack, stack.length * 2);
          for (int i = sp; i < stack.length; i++) {
            stack[i] = new OperationStackElement();
          }
        }
        OperationStackElement entry = stack[sp];
        entry.operation = operation;
        entry.sequenceNumber = this.operationSequenceNumber++;
        entry.childCount = 0;
        this.operationSp = sp + 1;
        return entry;
      }

      private OperationStackElement popOperation() {
        return this.operationStack[--this.operationSp];
      }

      private OperationStackElement peekOperation() {
        if (this.operationSp <= 0) {
          return null;
        }
        return this.operationStack[this.operationSp - 1];
      }

      private void updateMaxStackHeight(int stackHeight) {
        this.maxStackHeight = Math.max(this.maxStackHeight, stackHeight);
        if (this.maxStackHeight > Short.MAX_VALUE) {
          throw BytecodeEncodingException.create("Maximum stack height exceeded.");
        }
      }

      private void ensureBytecodeCapacity(int size) {
        if (size > this.bc.length) {
          this.bc = Arrays.copyOf(this.bc, Math.max(size, this.bc.length * 2));
        }
      }

      private int addConstant(Object constant) {
        return constants.add(constant);
      }

      /**
       * Allocates a slot for a constant which will be manually added to the constant pool later.
       */
      private short allocateConstantSlot() {
        return safeCastShort(constants.addNull());
      }

      private Object[] toConstants() {
        return constants.materialize();
      }

      private int allocateNode() {
        if (!this.reachable) {
          return -1;
        }
        return checkOverflowInt(this.numNodes++, "Node counter");
      }

      private short allocateBytecodeLocal() {
        return checkOverflowShort((short) this.numLocals++, "Number of locals");
      }

      private int allocateBranchProfile() {
        if (!this.reachable) {
          return -1;
        }
        return checkOverflowInt(this.numConditionalBranches++, "Number of branch profiles");
      }

      private int doEmitLocal(int localIndex, int frameIndex, Object name, Object info) {
        int nameIndex = -1;
        if (name != null) {
          nameIndex = this.addConstant(name);
        }
        int infoIndex = -1;
        if (info != null) {
          infoIndex = this.addConstant(info);
        }
        return doEmitLocal(localIndex, frameIndex, nameIndex, infoIndex);
      }

      private int doEmitLocal(int localIndex, int frameIndex, int nameIndex, int infoIndex) {
        int tableIndex = allocateLocalsTableEntry();
        assert frameIndex - USER_LOCALS_START_INDEX >= 0;
        this.locals[tableIndex + LOCALS_OFFSET_START_BCI] = this.bci;
        // will be patched later at the end of the block
        this.locals[tableIndex + LOCALS_OFFSET_END_BCI] = -1;
        this.locals[tableIndex + LOCALS_OFFSET_LOCAL_INDEX] = localIndex;
        this.locals[tableIndex + LOCALS_OFFSET_FRAME_INDEX] = frameIndex;
        this.locals[tableIndex + LOCALS_OFFSET_NAME] = nameIndex;
        this.locals[tableIndex + LOCALS_OFFSET_INFO] = infoIndex;
        return tableIndex;
      }

      private int allocateLocalsTableEntry() {
        int result = this.localsTableIndex;
        if (this.locals == null) {
          assert result == 0;
          this.locals = new int[LOCALS_LENGTH * 8];
        } else if (result + LOCALS_LENGTH > this.locals.length) {
          this.locals =
              Arrays.copyOf(this.locals, Math.max(result + LOCALS_LENGTH, this.locals.length * 2));
        }
        this.localsTableIndex += LOCALS_LENGTH;
        return result;
      }

      /**
       * Iterates the handler table, searching for unresolved entries corresponding to the given
       * handlerId. Patches them with the handlerBci and handlerSp now that those values are known.
       */
      private void patchHandlerTable(
          int tableStart, int tableEnd, int handlerId, int handlerBci, int handlerSp) {
        for (int i = tableStart; i < tableEnd; i += EXCEPTION_HANDLER_LENGTH) {
          if (this.handlerTable[i + EXCEPTION_HANDLER_OFFSET_KIND] != HANDLER_CUSTOM) {
            continue;
          }
          if (this.handlerTable[i + EXCEPTION_HANDLER_OFFSET_HANDLER_BCI] != -handlerId) {
            continue;
          }
          this.handlerTable[i + EXCEPTION_HANDLER_OFFSET_HANDLER_BCI] = handlerBci;
          this.handlerTable[i + EXCEPTION_HANDLER_OFFSET_HANDLER_SP] = handlerSp;
        }
      }

      private int doCreateExceptionHandler(
          int startBci, int endBci, int handlerKind, int handlerBci, int handlerSp) {
        assert startBci <= endBci;
        // Don't create empty handler ranges.
        if (startBci == endBci) {
          return UNINITIALIZED;
        }
        // If the previous entry is for the same handler and the ranges are contiguous, combine
        // them.
        if (this.handlerTableSize > 0) {
          int previousEntry = this.handlerTableSize - EXCEPTION_HANDLER_LENGTH;
          int previousEndBci = this.handlerTable[previousEntry + EXCEPTION_HANDLER_OFFSET_END_BCI];
          int previousKind = this.handlerTable[previousEntry + EXCEPTION_HANDLER_OFFSET_KIND];
          int previousHandlerBci =
              this.handlerTable[previousEntry + EXCEPTION_HANDLER_OFFSET_HANDLER_BCI];
          if (previousEndBci == startBci
              && previousKind == handlerKind
              && previousHandlerBci == handlerBci) {
            this.handlerTable[previousEntry + EXCEPTION_HANDLER_OFFSET_END_BCI] = endBci;
            return UNINITIALIZED;
          }
        }
        if (this.handlerTable.length <= this.handlerTableSize + EXCEPTION_HANDLER_LENGTH) {
          this.handlerTable = Arrays.copyOf(this.handlerTable, this.handlerTable.length * 2);
        }
        int result = this.handlerTableSize;
        this.handlerTable[result + EXCEPTION_HANDLER_OFFSET_START_BCI] = startBci;
        this.handlerTable[result + EXCEPTION_HANDLER_OFFSET_END_BCI] = endBci;
        this.handlerTable[result + EXCEPTION_HANDLER_OFFSET_KIND] = handlerKind;
        this.handlerTable[result + EXCEPTION_HANDLER_OFFSET_HANDLER_BCI] = handlerBci;
        this.handlerTable[result + EXCEPTION_HANDLER_OFFSET_HANDLER_SP] = handlerSp;
        this.handlerTableSize += EXCEPTION_HANDLER_LENGTH;
        return result;
      }

      private void doPatchSourceInfo(
          ArrayList<GettingStartedBytecodeRootNodeGen> nodes,
          int nodeId,
          int sourceIndex,
          int start,
          int length) {
        int[] info;
        if (nodeId >= 0) {
          info = nodes.get(nodeId).bytecode.sourceInfo;
        } else {
          info = this.sourceInfo;
        }
        int index = sourceIndex;
        while (index >= 0) {
          int oldStart = info[index + SOURCE_INFO_OFFSET_START];
          int oldEnd = info[index + SOURCE_INFO_OFFSET_LENGTH];
          assert nodeId >= 0 ? oldEnd == PATCH_NODE_SOURCE : oldEnd == PATCH_CURRENT_SOURCE;
          info[index + SOURCE_INFO_OFFSET_START] = start;
          info[index + SOURCE_INFO_OFFSET_LENGTH] = length;
          index = oldStart;
        }
      }

      private int doEmitSourceInfo(
          int sourceIndex, int startBci, int endBci, int start, int length) {
        if (this.rootOperationSp == -1) {
          return -1;
        }
        int index = this.sourceInfoIndex;
        int prevIndex = index - SOURCE_INFO_LENGTH;
        if (prevIndex >= 0
            && start >= -1
            && length >= -1
            && (this.sourceInfo[prevIndex + SOURCE_INFO_OFFSET_SOURCE]) == sourceIndex
            && (this.sourceInfo[prevIndex + SOURCE_INFO_OFFSET_START]) == start
            && (this.sourceInfo[prevIndex + SOURCE_INFO_OFFSET_LENGTH]) == length) {
          if ((this.sourceInfo[prevIndex + SOURCE_INFO_OFFSET_START_BCI]) == startBci
              && (this.sourceInfo[prevIndex + SOURCE_INFO_OFFSET_END_BCI]) == endBci) {
            // duplicate entry
            return prevIndex;
          } else if ((this.sourceInfo[prevIndex + SOURCE_INFO_OFFSET_END_BCI]) == startBci) {
            // contiguous entry
            this.sourceInfo[prevIndex + SOURCE_INFO_OFFSET_END_BCI] = endBci;
            return prevIndex;
          }
        }
        if (index >= this.sourceInfo.length) {
          this.sourceInfo = Arrays.copyOf(this.sourceInfo, this.sourceInfo.length * 2);
        }
        this.sourceInfo[index + SOURCE_INFO_OFFSET_START_BCI] = startBci;
        this.sourceInfo[index + SOURCE_INFO_OFFSET_END_BCI] = endBci;
        this.sourceInfo[index + SOURCE_INFO_OFFSET_SOURCE] = sourceIndex;
        this.sourceInfo[index + SOURCE_INFO_OFFSET_START] = start;
        this.sourceInfo[index + SOURCE_INFO_OFFSET_LENGTH] = length;
        this.sourceInfoIndex = index + SOURCE_INFO_LENGTH;
        return index;
      }

      private void registerUnresolvedLabel(BytecodeLabel label, int immediateBci) {
        BytecodeLabelImpl impl = (BytecodeLabelImpl) label;
        if (impl.unresolved0 == -1) {
          impl.unresolved0 = immediateBci;
          return;
        }
        if (impl.unresolved1 == -1) {
          impl.unresolved1 = immediateBci;
          return;
        }
        int index = impl.unresolvedCount++;
        int[] array = impl.unresolvedArray;
        if (array == null) {
          array = impl.unresolvedArray = new int[4];
        } else if (index >= array.length) {
          array = impl.unresolvedArray = Arrays.copyOf(array, array.length * 2);
        }
        array[index] = immediateBci;
      }

      private void resolveUnresolvedLabel(BytecodeLabel label, int stackHeight) {
        BytecodeLabelImpl impl = (BytecodeLabelImpl) label;
        assert !impl.isDefined();
        impl.bci = this.bci;
        if (impl.unresolved0 != -1) {
          BYTES.putInt(this.bc, impl.unresolved0, this.bci);
        }
        if (impl.unresolved1 != -1) {
          BYTES.putInt(this.bc, impl.unresolved1, this.bci);
        }
        for (int i = 0; i < impl.unresolvedCount; i++) {
          BYTES.putInt(this.bc, impl.unresolvedArray[i], this.bci);
        }
      }

      private boolean doEmitInstruction(short instruction, int stackEffect) {
        if (stackEffect != 0) {
          this.currentStackHeight += stackEffect;
          assert this.currentStackHeight >= 0;
        }
        if (stackEffect > 0) {
          this.updateMaxStackHeight(this.currentStackHeight);
        }
        if (!this.reachable) {
          return false;
        }
        int newBci = checkBci(this.bci + 2);
        if (newBci > this.bc.length) {
          this.ensureBytecodeCapacity(newBci);
        }
        BYTES.putShort(this.bc, this.bci + 0, instruction);
        this.bci = newBci;
        return true;
      }

      private boolean doEmitInstructionS(short instruction, int stackEffect, short data0) {
        if (stackEffect != 0) {
          this.currentStackHeight += stackEffect;
          assert this.currentStackHeight >= 0;
        }
        if (stackEffect > 0) {
          this.updateMaxStackHeight(this.currentStackHeight);
        }
        if (!this.reachable) {
          return false;
        }
        int newBci = checkBci(this.bci + 4);
        if (newBci > this.bc.length) {
          this.ensureBytecodeCapacity(newBci);
        }
        BYTES.putShort(this.bc, this.bci + 0, instruction);
        BYTES.putShort(this.bc, this.bci + 2 /* imm 0 */, data0);
        this.bci = newBci;
        return true;
      }

      private boolean doEmitInstructionI(short instruction, int stackEffect, int data0) {
        if (stackEffect != 0) {
          this.currentStackHeight += stackEffect;
          assert this.currentStackHeight >= 0;
        }
        if (stackEffect > 0) {
          this.updateMaxStackHeight(this.currentStackHeight);
        }
        if (!this.reachable) {
          return false;
        }
        int newBci = checkBci(this.bci + 6);
        if (newBci > this.bc.length) {
          this.ensureBytecodeCapacity(newBci);
        }
        BYTES.putShort(this.bc, this.bci + 0, instruction);
        BYTES.putInt(this.bc, this.bci + 2 /* imm 0 */, data0);
        this.bci = newBci;
        return true;
      }

      private boolean doEmitInstructionII(
          short instruction, int stackEffect, int data0, int data1) {
        if (stackEffect != 0) {
          this.currentStackHeight += stackEffect;
          assert this.currentStackHeight >= 0;
        }
        if (stackEffect > 0) {
          this.updateMaxStackHeight(this.currentStackHeight);
        }
        if (!this.reachable) {
          return false;
        }
        int newBci = checkBci(this.bci + 10);
        if (newBci > this.bc.length) {
          this.ensureBytecodeCapacity(newBci);
        }
        BYTES.putShort(this.bc, this.bci + 0, instruction);
        BYTES.putInt(this.bc, this.bci + 2 /* imm 0 */, data0);
        BYTES.putInt(this.bc, this.bci + 6 /* imm 1 */, data1);
        this.bci = newBci;
        return true;
      }

      @Override
      public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(RootStackElement.class.getSimpleName());
        b.append("[");
        b.append("bytecodeIndex=").append(this.bci);
        b.append(", stackPointer=").append(this.currentStackHeight);
        b.append(", operations=");
        b.append(System.lineSeparator());
        for (int i = this.operationSp - 1; i >= 0; i--) {
          b.append("    [");
          b.append(String.format("%03d", i));
          b.append("] ");
          b.append(this.operationStack[i].toString());
          b.append(System.lineSeparator());
        }
        b.append("    instructions=");
        b.append(System.lineSeparator());
        Object[] tempConstants = constants.create();
        byte[] tempBytecodes = Arrays.copyOf(this.bc, this.bci);
        for (int currentBci = 0; currentBci < bci; ) {
          int opcode = BYTES.getShort(this.bc, currentBci);
          b.append("       ");
          b.append(new InstructionImpl(null, currentBci, opcode, tempBytecodes, tempConstants));
          b.append(System.lineSeparator());
          currentBci = currentBci + Instructions.getLength(opcode);
        }
        if (parent != null) {
          b.append("  parent=");
          b.append(parent.toString());
        }
        return b.toString();
      }

      static RootStackElement acquire() {
        SoftReference<RootStackElement> ref = THREAD_LOCAL.get();
        if (ref != null) {
          THREAD_LOCAL.set(null);
          RootStackElement obj = ref.get();
          if (obj != null) {
            return obj;
          }
        }
        return new RootStackElement(null);
      }

      static void release(RootStackElement obj) {
        obj.cleanup();
        THREAD_LOCAL.set(obj.reference);
      }
    }

    private static final class OperationStackElement {

      int operation;
      int sequenceNumber;
      int childCount;

      /**
       * Field mappings: endBranchFixupBci : int IFTHENELSE, CONDITIONAL, WHILE, TRYCATCH,
       * TRYFINALLY, TRYCATCHOTHERWISE, sourceIndex : int SOURCE, SOURCESECTIONPREFIX,
       * SOURCESECTIONSUFFIX, frameOffset : int BLOCK, ROOT, finallyOperationSp : int The index of
       * the corresponding finally operation (TryFinally/TryCatchOtherwise) on the operation stack.
       * This index can be used to skip over the "try" body operations when walking the operation
       * stack from top to bottom. FINALLYHANDLER, numBranchFixupBcis : int SCOR,
       */
      private int int0;

      /**
       * Field mappings: falseBranchFixupBci : int IFTHEN, IFTHENELSE, CONDITIONAL, handlerId : int
       * TRYCATCH, TRYFINALLY, TRYCATCHOTHERWISE, numLocals : int BLOCK, ROOT, startBci : int
       * SOURCESECTIONPREFIX, SOURCESECTIONSUFFIX, whileStartBci : int WHILE,
       */
      private int int1;

      /**
       * Field mappings: stackHeight : int TRYCATCH, TRYFINALLY, TRYCATCHOTHERWISE, start : int
       * SOURCESECTIONPREFIX, SOURCESECTIONSUFFIX, startStackHeight : int BLOCK, index : int ROOT,
       */
      private int int2;

      /**
       * Field mappings: tryStartBci : int TRYCATCH, TRYFINALLY, TRYCATCHOTHERWISE, length : int
       * SOURCESECTIONPREFIX, SOURCESECTIONSUFFIX,
       */
      private int int3;

      /**
       * Field mappings: extraTableEntriesStart : int TRYCATCH, TRYFINALLY, TRYCATCHOTHERWISE,
       * sourceNodeId : int SOURCESECTIONPREFIX, SOURCESECTIONSUFFIX,
       */
      private int int4;

      /** Field mappings: extraTableEntriesEnd : int TRYCATCH, TRYFINALLY, TRYCATCHOTHERWISE, */
      private int int5;

      /**
       * Field mappings: finallyHandlerSp : int The index of the finally handler operation on the
       * operation stack. This value is uninitialized unless a finally handler is being emitted, and
       * allows us to walk the operation stack from bottom to top. TRYFINALLY, TRYCATCHOTHERWISE,
       */
      private int int6;

      /**
       * Field mappings: producedValue : boolean BLOCK, ROOT, RETURN, SOURCE, SOURCESECTIONPREFIX,
       * SOURCESECTIONSUFFIX, thenReachable : boolean IFTHEN, IFTHENELSE, CONDITIONAL,
       * operationReachable : boolean TRYCATCH, TRYFINALLY, TRYCATCHOTHERWISE, bodyReachable :
       * boolean WHILE,
       */
      private boolean boolean0;

      /**
       * Field mappings: tryReachable : boolean TRYCATCH, TRYFINALLY, TRYCATCHOTHERWISE, valid :
       * boolean BLOCK, ROOT, elseReachable : boolean IFTHENELSE, CONDITIONAL,
       */
      private boolean boolean1;

      /**
       * Field mappings: catchReachable : boolean TRYCATCH, TRYFINALLY, TRYCATCHOTHERWISE, reachable
       * : boolean ROOT,
       */
      private boolean boolean2;

      /** Field mappings: locals : int[] BLOCK, ROOT, branchFixupBcis : int[] SCOR, */
      private int[] intArray0;

      /**
       * Field mappings: declaredLabels : ArrayList<BytecodeLabel> BLOCK, ROOT, finallyGenerator :
       * Runnable TRYFINALLY, TRYCATCHOTHERWISE, local : BytecodeLocalImpl LOADLOCAL, STORELOCAL,
       */
      private Object object0;

      OperationStackElement() {
        this.intArray0 = new int[16];
      }

      int getStartStackHeight() {
        return int2;
      }

      void setStartStackHeight(int value) {
        this.int2 = value;
      }

      boolean getProducedValue() {
        return boolean0;
      }

      void setProducedValue(boolean value) {
        this.boolean0 = value;
      }

      int getFrameOffset() {
        return int0;
      }

      void setFrameOffset(int value) {
        this.int0 = value;
      }

      int[] getLocals() {
        return intArray0;
      }

      void setLocals(int[] value) {
        this.intArray0 = value;
      }

      int getNumLocals() {
        return int1;
      }

      void setNumLocals(int value) {
        this.int1 = value;
      }

      boolean getValid() {
        return boolean1;
      }

      void setValid(boolean value) {
        this.boolean1 = value;
      }

      @SuppressWarnings("unchecked")
      ArrayList<BytecodeLabel> getDeclaredLabels() {
        return (ArrayList<BytecodeLabel>) object0;
      }

      void setDeclaredLabels(ArrayList<BytecodeLabel> value) {
        this.object0 = value;
      }

      int getIndex() {
        return int2;
      }

      void setIndex(int value) {
        this.int2 = value;
      }

      boolean getReachable() {
        return boolean2;
      }

      void setReachable(boolean value) {
        this.boolean2 = value;
      }

      boolean getThenReachable() {
        return boolean0;
      }

      void setThenReachable(boolean value) {
        this.boolean0 = value;
      }

      int getFalseBranchFixupBci() {
        return int1;
      }

      void setFalseBranchFixupBci(int value) {
        this.int1 = value;
      }

      boolean getElseReachable() {
        return boolean1;
      }

      void setElseReachable(boolean value) {
        this.boolean1 = value;
      }

      int getEndBranchFixupBci() {
        return int0;
      }

      void setEndBranchFixupBci(int value) {
        this.int0 = value;
      }

      int getWhileStartBci() {
        return int1;
      }

      void setWhileStartBci(int value) {
        this.int1 = value;
      }

      boolean getBodyReachable() {
        return boolean0;
      }

      void setBodyReachable(boolean value) {
        this.boolean0 = value;
      }

      int getHandlerId() {
        return int1;
      }

      void setHandlerId(int value) {
        this.int1 = value;
      }

      int getStackHeight() {
        return int2;
      }

      void setStackHeight(int value) {
        this.int2 = value;
      }

      int getTryStartBci() {
        return int3;
      }

      void setTryStartBci(int value) {
        this.int3 = value;
      }

      boolean getOperationReachable() {
        return boolean0;
      }

      void setOperationReachable(boolean value) {
        this.boolean0 = value;
      }

      boolean getTryReachable() {
        return boolean1;
      }

      void setTryReachable(boolean value) {
        this.boolean1 = value;
      }

      boolean getCatchReachable() {
        return boolean2;
      }

      void setCatchReachable(boolean value) {
        this.boolean2 = value;
      }

      int getExtraTableEntriesStart() {
        return int4;
      }

      void setExtraTableEntriesStart(int value) {
        this.int4 = value;
      }

      int getExtraTableEntriesEnd() {
        return int5;
      }

      void setExtraTableEntriesEnd(int value) {
        this.int5 = value;
      }

      Runnable getFinallyGenerator() {
        return (Runnable) object0;
      }

      void setFinallyGenerator(Runnable value) {
        this.object0 = value;
      }

      /**
       * The index of the finally handler operation on the operation stack. This value is
       * uninitialized unless a finally handler is being emitted, and allows us to walk the
       * operation stack from bottom to top.
       */
      int getFinallyHandlerSp() {
        return int6;
      }

      /**
       * The index of the finally handler operation on the operation stack. This value is
       * uninitialized unless a finally handler is being emitted, and allows us to walk the
       * operation stack from bottom to top.
       */
      void setFinallyHandlerSp(int value) {
        this.int6 = value;
      }

      /**
       * The index of the corresponding finally operation (TryFinally/TryCatchOtherwise) on the
       * operation stack. This index can be used to skip over the "try" body operations when walking
       * the operation stack from top to bottom.
       */
      int getFinallyOperationSp() {
        return int0;
      }

      /**
       * The index of the corresponding finally operation (TryFinally/TryCatchOtherwise) on the
       * operation stack. This index can be used to skip over the "try" body operations when walking
       * the operation stack from top to bottom.
       */
      void setFinallyOperationSp(int value) {
        this.int0 = value;
      }

      BytecodeLocalImpl getLocal() {
        return (BytecodeLocalImpl) object0;
      }

      void setLocal(BytecodeLocalImpl value) {
        this.object0 = value;
      }

      int getSourceIndex() {
        return int0;
      }

      void setSourceIndex(int value) {
        this.int0 = value;
      }

      int getStartBci() {
        return int1;
      }

      void setStartBci(int value) {
        this.int1 = value;
      }

      int getStart() {
        return int2;
      }

      void setStart(int value) {
        this.int2 = value;
      }

      int getLength() {
        return int3;
      }

      void setLength(int value) {
        this.int3 = value;
      }

      int getSourceNodeId() {
        return int4;
      }

      void setSourceNodeId(int value) {
        this.int4 = value;
      }

      int[] getBranchFixupBcis() {
        return intArray0;
      }

      void setBranchFixupBcis(int[] value) {
        this.intArray0 = value;
      }

      int getNumBranchFixupBcis() {
        return int0;
      }

      void setNumBranchFixupBcis(int value) {
        this.int0 = value;
      }

      boolean validateDeclaredLabels() {
        var labels = getDeclaredLabels();
        if (labels != null) {
          for (BytecodeLabel label : labels) {
            BytecodeLabelImpl impl = (BytecodeLabelImpl) label;
            if (!impl.isDefined()) {
              return false;
            }
          }
        }
        return true;
      }

      void addDeclaredLabel(BytecodeLabel label) {
        var labels = getDeclaredLabels();
        if (labels == null) {
          labels = new ArrayList<>(8);
          setDeclaredLabels(labels);
        }
        labels.add(label);
      }

      void registerLocal(int tableIndex) {
        int localTableIndex = getNumLocals();
        setNumLocals(localTableIndex + 1);
        int[] localsTable = getLocals();
        if (localsTable == null) {
          localsTable = new int[8];
          setLocals(localsTable);
        } else if (localTableIndex >= localsTable.length) {
          localsTable = Arrays.copyOf(localsTable, localsTable.length * 2);
          setLocals(localsTable);
        }
        localsTable[localTableIndex] = tableIndex;
      }

      @Override
      public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(String.format("%-15s", Operations.getName(operation)));
        b.append(" ");
        switch (operation) {
          case Operations.BLOCK:
            b.append("startStackHeight(")
                .append(this.getStartStackHeight())
                .append(")")
                .append(" ");
            b.append("producedValue(").append(this.getProducedValue()).append(")").append(" ");
            b.append("frameOffset(").append(this.getFrameOffset()).append(")").append(" ");
            b.append("locals(")
                .append(Arrays.toString(Arrays.copyOf(this.getLocals(), this.getNumLocals())))
                .append(")")
                .append(" ");
            b.append("valid(").append(this.getValid()).append(")").append(" ");
            b.append("declaredLabels(").append(this.getDeclaredLabels()).append(")");
            break;
          case Operations.ROOT:
            b.append("index(").append(this.getIndex()).append(")").append(" ");
            b.append("producedValue(").append(this.getProducedValue()).append(")").append(" ");
            b.append("reachable(").append(this.getReachable()).append(")").append(" ");
            b.append("frameOffset(").append(this.getFrameOffset()).append(")").append(" ");
            b.append("locals(")
                .append(Arrays.toString(Arrays.copyOf(this.getLocals(), this.getNumLocals())))
                .append(")")
                .append(" ");
            b.append("valid(").append(this.getValid()).append(")").append(" ");
            b.append("declaredLabels(").append(this.getDeclaredLabels()).append(")");
            break;
          case Operations.IFTHEN:
            b.append("thenReachable(").append(this.getThenReachable()).append(")").append(" ");
            b.append("falseBranchFixupBci(").append(this.getFalseBranchFixupBci()).append(")");
            break;
          case Operations.IFTHENELSE:
          case Operations.CONDITIONAL:
            b.append("thenReachable(").append(this.getThenReachable()).append(")").append(" ");
            b.append("elseReachable(").append(this.getElseReachable()).append(")").append(" ");
            b.append("falseBranchFixupBci(")
                .append(this.getFalseBranchFixupBci())
                .append(")")
                .append(" ");
            b.append("endBranchFixupBci(").append(this.getEndBranchFixupBci()).append(")");
            break;
          case Operations.WHILE:
            b.append("whileStartBci(").append(this.getWhileStartBci()).append(")").append(" ");
            b.append("bodyReachable(").append(this.getBodyReachable()).append(")").append(" ");
            b.append("endBranchFixupBci(").append(this.getEndBranchFixupBci()).append(")");
            break;
          case Operations.TRYCATCH:
            b.append("handlerId(").append(this.getHandlerId()).append(")").append(" ");
            b.append("stackHeight(").append(this.getStackHeight()).append(")").append(" ");
            b.append("tryStartBci(").append(this.getTryStartBci()).append(")").append(" ");
            b.append("operationReachable(")
                .append(this.getOperationReachable())
                .append(")")
                .append(" ");
            b.append("tryReachable(").append(this.getTryReachable()).append(")").append(" ");
            b.append("catchReachable(").append(this.getCatchReachable()).append(")").append(" ");
            b.append("endBranchFixupBci(")
                .append(this.getEndBranchFixupBci())
                .append(")")
                .append(" ");
            b.append("extraTableEntriesStart(")
                .append(this.getExtraTableEntriesStart())
                .append(")")
                .append(" ");
            b.append("extraTableEntriesEnd(").append(this.getExtraTableEntriesEnd()).append(")");
            break;
          case Operations.TRYFINALLY:
          case Operations.TRYCATCHOTHERWISE:
            b.append("handlerId(").append(this.getHandlerId()).append(")").append(" ");
            b.append("stackHeight(").append(this.getStackHeight()).append(")").append(" ");
            b.append("finallyGenerator(")
                .append(this.getFinallyGenerator())
                .append(")")
                .append(" ");
            b.append("tryStartBci(").append(this.getTryStartBci()).append(")").append(" ");
            b.append("operationReachable(")
                .append(this.getOperationReachable())
                .append(")")
                .append(" ");
            b.append("tryReachable(").append(this.getTryReachable()).append(")").append(" ");
            b.append("catchReachable(").append(this.getCatchReachable()).append(")").append(" ");
            b.append("endBranchFixupBci(")
                .append(this.getEndBranchFixupBci())
                .append(")")
                .append(" ");
            b.append("extraTableEntriesStart(")
                .append(this.getExtraTableEntriesStart())
                .append(")")
                .append(" ");
            b.append("extraTableEntriesEnd(")
                .append(this.getExtraTableEntriesEnd())
                .append(")")
                .append(" ");
            b.append("finallyHandlerSp(").append(this.getFinallyHandlerSp()).append(")");
            break;
          case Operations.FINALLYHANDLER:
            b.append("finallyOperationSp(").append(this.getFinallyOperationSp()).append(")");
            break;
          case Operations.STORELOCAL:
            b.append("local(").append(this.getLocal()).append(")");
            break;
          case Operations.RETURN:
            b.append("producedValue(").append(this.getProducedValue()).append(")");
            break;
          case Operations.SOURCE:
            b.append("sourceIndex(").append(this.getSourceIndex()).append(")").append(" ");
            b.append("producedValue(").append(this.getProducedValue()).append(")");
            break;
          case Operations.SOURCESECTIONPREFIX:
          case Operations.SOURCESECTIONSUFFIX:
            b.append("sourceIndex(").append(this.getSourceIndex()).append(")").append(" ");
            b.append("startBci(").append(this.getStartBci()).append(")").append(" ");
            b.append("start(").append(this.getStart()).append(")").append(" ");
            b.append("length(").append(this.getLength()).append(")").append(" ");
            b.append("sourceNodeId(").append(this.getSourceNodeId()).append(")").append(" ");
            b.append("producedValue(").append(this.getProducedValue()).append(")");
            break;
          case Operations.ADD:
          case Operations.DIV:
          case Operations.EQUALS:
          case Operations.LESSTHAN:
          case Operations.EAGEROR:
          case Operations.TOBOOL:
          case Operations.ARRAYLENGTH:
          case Operations.ARRAYINDEX:
            break;
          case Operations.SCOR:
            b.append("branchFixupBcis(")
                .append(
                    Arrays.toString(
                        Arrays.copyOf(this.getBranchFixupBcis(), this.getNumBranchFixupBcis())))
                .append(")");
            break;
        }
        return b.toString();
      }
    }

    private static final class BytecodeLocalImpl extends BytecodeLocal {

      private final short frameIndex;
      private final short localIndex;
      private final short rootIndex;
      private final OperationStackElement scope;
      private final int scopeSequenceNumber;

      BytecodeLocalImpl(
          short frameIndex,
          short localIndex,
          short rootIndex,
          OperationStackElement scope,
          int scopeSequenceNumber) {
        super(BytecodeRootNodesImpl.VISIBLE_TOKEN);
        this.frameIndex = frameIndex;
        this.localIndex = localIndex;
        this.rootIndex = rootIndex;
        this.scope = scope;
        this.scopeSequenceNumber = scopeSequenceNumber;
      }

      @Override
      public int getLocalOffset() {
        return frameIndex - USER_LOCALS_START_INDEX;
      }

      @Override
      public int getLocalIndex() {
        return localIndex;
      }

      @Override
      public String toString() {
        return "BytecodeLocal[localOffset="
            + this.getLocalOffset()
            + ", localIndex="
            + this.getLocalIndex()
            + ", rootIndex="
            + rootIndex
            + "]";
      }
    }

    private static final class BytecodeLabelImpl extends BytecodeLabel {

      private final int id;
      int bci;
      private final int declaringOp;
      int unresolved0 = -1;
      int unresolved1 = -1;
      int unresolvedCount;
      int[] unresolvedArray;

      BytecodeLabelImpl(int id, int bci, int declaringOp) {
        super(BytecodeRootNodesImpl.VISIBLE_TOKEN);
        this.id = id;
        this.bci = bci;
        this.declaringOp = declaringOp;
      }

      public boolean isDefined() {
        return bci != -1;
      }

      @Override
      public boolean equals(Object other) {
        if (!(other instanceof BytecodeLabelImpl)) {
          return false;
        }
        return this.id == ((BytecodeLabelImpl) other).id;
      }

      @Override
      public int hashCode() {
        return this.id;
      }

      @Override
      public String toString() {
        return "BytecodeLabel[id="
            + this.id
            + (!isDefined() ? ", undefined" : ", bci=" + this.bci)
            + "]";
      }
    }
  }

  private static final class BytecodeConfigEncoderImpl extends BytecodeConfigEncoder {

    private static final BytecodeConfigEncoderImpl INSTANCE = new BytecodeConfigEncoderImpl();

    private BytecodeConfigEncoderImpl() {
      super(BytecodeRootNodesImpl.VISIBLE_TOKEN);
    }

    @Override
    protected long encodeInstrumentation(Class<?> c) throws IllegalArgumentException {
      throw new IllegalArgumentException(
          String.format(
              "Invalid instrumentation specified. Instrumentation '%s' does not exist or is not an instrumentation for 'website.lihan.temu.GettingStarted.GettingStartedBytecodeRootNode'. Instrumentations can be specified using the @Instrumentation annotation.",
              c.getName()));
    }

    @Override
    protected long encodeTag(Class<?> c) throws IllegalArgumentException {
      return ((long) CLASS_TO_TAG_MASK.get(c)) << 32;
    }

    private static long decode(BytecodeConfig config) {
      return decode(getEncoder(config), getEncoding(config));
    }

    private static long decode(BytecodeConfigEncoder encoder, long encoding) {
      if (encoder != null && encoder != BytecodeConfigEncoderImpl.INSTANCE) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new IllegalArgumentException(
            "Encoded config is not compatible with this bytecode node.");
      }
      return (encoding & 0xf00000001L);
    }
  }

  private static final class BytecodeRootNodesImpl
      extends BytecodeRootNodes<GettingStartedBytecodeRootNode> {

    private static final Object VISIBLE_TOKEN = TOKEN;

    @CompilationFinal private volatile long encoding;

    BytecodeRootNodesImpl(
        BytecodeParser<website.lihan.temu.GettingStartedBytecodeRootNodeGen.Builder> generator,
        BytecodeConfig config) {
      super(VISIBLE_TOKEN, generator);
      this.encoding = BytecodeConfigEncoderImpl.decode(config);
    }

    @Override
    @SuppressWarnings("hiding")
    protected boolean updateImpl(BytecodeConfigEncoder encoder, long encoding) {
      long maskedEncoding = BytecodeConfigEncoderImpl.decode(encoder, encoding);
      long oldEncoding = this.encoding;
      long newEncoding = maskedEncoding | oldEncoding;
      if ((oldEncoding | newEncoding) == oldEncoding) {
        return false;
      }
      CompilerDirectives.transferToInterpreterAndInvalidate();
      return performUpdate(maskedEncoding);
    }

    private synchronized boolean performUpdate(long maskedEncoding) {
      CompilerAsserts.neverPartOfCompilation();
      long oldEncoding = this.encoding;
      long newEncoding = maskedEncoding | oldEncoding;
      if ((oldEncoding | newEncoding) == oldEncoding) {
        // double checked locking
        return false;
      }
      boolean oldSources = (oldEncoding & 0b1) != 0;
      int oldInstrumentations = (int) ((oldEncoding >> 1) & 0x7FFF_FFFF);
      int oldTags = (int) ((oldEncoding >> 32) & 0xFFFF_FFFF);
      boolean newSources = (newEncoding & 0b1) != 0;
      int newInstrumentations = (int) ((newEncoding >> 1) & 0x7FFF_FFFF);
      int newTags = (int) ((newEncoding >> 32) & 0xFFFF_FFFF);
      boolean needsBytecodeReparse =
          newInstrumentations != oldInstrumentations || newTags != oldTags;
      boolean needsSourceReparse = newSources != oldSources || (needsBytecodeReparse && newSources);
      if (!needsBytecodeReparse && !needsSourceReparse) {
        return false;
      }
      BytecodeParser<website.lihan.temu.GettingStartedBytecodeRootNodeGen.Builder> parser =
          getParserImpl();
      UpdateReason reason =
          new UpdateReason(
              oldSources != newSources,
              newInstrumentations & ~oldInstrumentations,
              newTags & ~oldTags);
      Builder builder =
          new Builder(
              this, needsBytecodeReparse, newTags, newInstrumentations, needsSourceReparse, reason);
      for (GettingStartedBytecodeRootNode node : nodes) {
        builder.builtNodes.add((GettingStartedBytecodeRootNodeGen) node);
      }
      parser.parse(builder);
      builder.finish();
      this.encoding = newEncoding;
      return true;
    }

    private void setNodes(GettingStartedBytecodeRootNodeGen[] nodes) {
      if (this.nodes != null) {
        throw new AssertionError();
      }
      this.nodes = nodes;
      for (GettingStartedBytecodeRootNodeGen node : nodes) {
        if (node.getRootNodes() != this) {
          throw new AssertionError();
        }
        if (node != nodes[node.buildIndex]) {
          throw new AssertionError();
        }
      }
    }

    @SuppressWarnings("unchecked")
    private BytecodeParser<website.lihan.temu.GettingStartedBytecodeRootNodeGen.Builder>
        getParserImpl() {
      return (BytecodeParser<website.lihan.temu.GettingStartedBytecodeRootNodeGen.Builder>)
          super.getParser();
    }

    private boolean validate() {
      for (GettingStartedBytecodeRootNode node : nodes) {
        ((GettingStartedBytecodeRootNodeGen) node).getBytecodeNodeImpl().validateBytecodes();
      }
      return true;
    }

    private BytecodeDSLTestLanguage getLanguage() {
      if (nodes.length == 0) {
        return null;
      }
      return nodes[0].getLanguage(BytecodeDSLTestLanguage.class);
    }

    private boolean isParsed() {
      return nodes != null;
    }

    private static final class UpdateReason implements CharSequence {

      private final boolean newSources;
      private final int newInstrumentations;
      private final int newTags;

      UpdateReason(boolean newSources, int newInstrumentations, int newTags) {
        this.newSources = newSources;
        this.newInstrumentations = newInstrumentations;
        this.newTags = newTags;
      }

      @Override
      public int length() {
        return toString().length();
      }

      @Override
      public char charAt(int index) {
        return toString().charAt(index);
      }

      @Override
      public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
      }

      @Override
      public String toString() {
        StringBuilder message = new StringBuilder();
        message.append("GettingStartedBytecodeRootNode requested ");
        String sep = "";
        if (newSources) {
          message.append("SourceInformation");
          sep = ", ";
        }
        if (newTags != 0) {
          if ((newTags & 0x1) != 0) {
            message.append(sep);
            message.append("Tag[RootTag]");
            sep = ", ";
          }
          if ((newTags & 0x2) != 0) {
            message.append(sep);
            message.append("Tag[RootBodyTag]");
            sep = ", ";
          }
          if ((newTags & 0x4) != 0) {
            message.append(sep);
            message.append("Tag[ExpressionTag]");
            sep = ", ";
          }
          if ((newTags & 0x8) != 0) {
            message.append(sep);
            message.append("Tag[StatementTag]");
            sep = ", ";
          }
        }
        message.append(".");
        return message.toString();
      }
    }
  }

  private static final class Instructions {

    /*
     * Instruction load.argument
     * kind: LOAD_ARGUMENT
     * encoding: [1 : short, index (short) : short]
     * signature: Object ()
     */
    private static final short LOAD_ARGUMENT = 1;
    /*
     * Instruction load.constant
     * kind: LOAD_CONSTANT
     * encoding: [2 : short, constant (const) : int]
     * signature: Object ()
     */
    private static final short LOAD_CONSTANT = 2;
    /*
     * Instruction load.local
     * kind: LOAD_LOCAL
     * encoding: [3 : short, frame_index : short]
     * signature: Object ()
     */
    private static final short LOAD_LOCAL = 3;
    /*
     * Instruction clear.local
     * kind: CLEAR_LOCAL
     * encoding: [4 : short, frame_index : short]
     * signature: void ()
     */
    private static final short CLEAR_LOCAL = 4;
    /*
     * Instruction store.local
     * kind: STORE_LOCAL
     * encoding: [5 : short, frame_index : short]
     * signature: void (Object)
     */
    private static final short STORE_LOCAL = 5;
    /*
     * Instruction branch
     * kind: BRANCH
     * encoding: [6 : short, branch_target (bci) : int]
     * signature: void ()
     */
    private static final short BRANCH = 6;
    /*
     * Instruction branch.backward
     * kind: BRANCH_BACKWARD
     * encoding: [7 : short, branch_target (bci) : int, loop_header_branch_profile (branch_profile) : int]
     * signature: void ()
     */
    private static final short BRANCH_BACKWARD = 7;
    /*
     * Instruction branch.false
     * kind: BRANCH_FALSE
     * encoding: [8 : short, branch_target (bci) : int, branch_profile : int]
     * signature: void (Object)
     */
    private static final short BRANCH_FALSE = 8;
    /*
     * Instruction pop
     * kind: POP
     * encoding: [9 : short]
     * signature: void (Object)
     */
    private static final short POP = 9;
    /*
     * Instruction dup
     * kind: DUP
     * encoding: [10 : short]
     * signature: void ()
     */
    private static final short DUP = 10;
    /*
     * Instruction load.null
     * kind: LOAD_NULL
     * encoding: [11 : short]
     * signature: Object ()
     */
    private static final short LOAD_NULL = 11;
    /*
     * Instruction return
     * kind: RETURN
     * encoding: [12 : short]
     * signature: void (Object)
     */
    private static final short RETURN = 12;
    /*
     * Instruction throw
     * kind: THROW
     * encoding: [13 : short]
     * signature: void (Object)
     */
    private static final short THROW = 13;
    /*
     * Instruction load.exception
     * kind: LOAD_EXCEPTION
     * encoding: [14 : short, exception_sp (sp) : short]
     * signature: Object ()
     */
    private static final short LOAD_EXCEPTION = 14;
    /*
     * Instruction c.Add
     * kind: CUSTOM
     * encoding: [15 : short, node : int]
     * nodeType: Add
     * signature: int (int, int)
     */
    private static final short ADD_ = 15;
    /*
     * Instruction c.Div
     * kind: CUSTOM
     * encoding: [16 : short, node : int]
     * nodeType: Div
     * signature: int (int, int)
     */
    private static final short DIV_ = 16;
    /*
     * Instruction c.Equals
     * kind: CUSTOM
     * encoding: [17 : short, node : int]
     * nodeType: Equals
     * signature: boolean (int, int)
     */
    private static final short EQUALS_ = 17;
    /*
     * Instruction c.LessThan
     * kind: CUSTOM
     * encoding: [18 : short, node : int]
     * nodeType: LessThan
     * signature: boolean (int, int)
     */
    private static final short LESS_THAN_ = 18;
    /*
     * Instruction c.EagerOr
     * kind: CUSTOM
     * encoding: [19 : short, node : int]
     * nodeType: EagerOr
     * signature: boolean (boolean, boolean)
     */
    private static final short EAGER_OR_ = 19;
    /*
     * Instruction c.ToBool
     * kind: CUSTOM
     * encoding: [20 : short, node : int]
     * nodeType: ToBool
     * signature: boolean (Object)
     */
    private static final short TO_BOOL_ = 20;
    /*
     * Instruction c.ArrayLength
     * kind: CUSTOM
     * encoding: [21 : short, node : int]
     * nodeType: ArrayLength
     * signature: int (int[])
     */
    private static final short ARRAY_LENGTH_ = 21;
    /*
     * Instruction c.ArrayIndex
     * kind: CUSTOM
     * encoding: [22 : short, node : int]
     * nodeType: ArrayIndex
     * signature: int (int[], int)
     */
    private static final short ARRAY_INDEX_ = 22;
    /*
     * Instruction sc.ScOr
     * kind: CUSTOM_SHORT_CIRCUIT
     * encoding: [23 : short, branch_target (bci) : int, branch_profile : int]
     * signature: boolean (boolean, boolean)
     */
    private static final short SC_OR_ = 23;
    /*
     * Instruction invalidate0
     * kind: INVALIDATE
     * encoding: [24 : short]
     * signature: void ()
     */
    private static final short INVALIDATE0 = 24;
    /*
     * Instruction invalidate1
     * kind: INVALIDATE
     * encoding: [25 : short, invalidated0 (short) : short]
     * signature: void ()
     */
    private static final short INVALIDATE1 = 25;
    /*
     * Instruction invalidate2
     * kind: INVALIDATE
     * encoding: [26 : short, invalidated0 (short) : short, invalidated1 (short) : short]
     * signature: void ()
     */
    private static final short INVALIDATE2 = 26;
    /*
     * Instruction invalidate3
     * kind: INVALIDATE
     * encoding: [27 : short, invalidated0 (short) : short, invalidated1 (short) : short, invalidated2 (short) : short]
     * signature: void ()
     */
    private static final short INVALIDATE3 = 27;
    /*
     * Instruction invalidate4
     * kind: INVALIDATE
     * encoding: [28 : short, invalidated0 (short) : short, invalidated1 (short) : short, invalidated2 (short) : short, invalidated3 (short) : short]
     * signature: void ()
     */
    private static final short INVALIDATE4 = 28;

    private static int getLength(int opcode) {
      switch (opcode) {
        case Instructions.POP:
        case Instructions.DUP:
        case Instructions.LOAD_NULL:
        case Instructions.RETURN:
        case Instructions.THROW:
        case Instructions.INVALIDATE0:
          return 2;
        case Instructions.LOAD_ARGUMENT:
        case Instructions.LOAD_LOCAL:
        case Instructions.CLEAR_LOCAL:
        case Instructions.STORE_LOCAL:
        case Instructions.LOAD_EXCEPTION:
        case Instructions.INVALIDATE1:
          return 4;
        case Instructions.LOAD_CONSTANT:
        case Instructions.BRANCH:
        case Instructions.ADD_:
        case Instructions.DIV_:
        case Instructions.EQUALS_:
        case Instructions.LESS_THAN_:
        case Instructions.EAGER_OR_:
        case Instructions.TO_BOOL_:
        case Instructions.ARRAY_LENGTH_:
        case Instructions.ARRAY_INDEX_:
        case Instructions.INVALIDATE2:
          return 6;
        case Instructions.INVALIDATE3:
          return 8;
        case Instructions.BRANCH_BACKWARD:
        case Instructions.BRANCH_FALSE:
        case Instructions.SC_OR_:
        case Instructions.INVALIDATE4:
          return 10;
      }
      throw CompilerDirectives.shouldNotReachHere("Invalid opcode");
    }

    private static String getName(int opcode) {
      switch (opcode) {
        case Instructions.LOAD_ARGUMENT:
          return "load.argument";
        case Instructions.LOAD_CONSTANT:
          return "load.constant";
        case Instructions.LOAD_LOCAL:
          return "load.local";
        case Instructions.CLEAR_LOCAL:
          return "clear.local";
        case Instructions.STORE_LOCAL:
          return "store.local";
        case Instructions.BRANCH:
          return "branch";
        case Instructions.BRANCH_BACKWARD:
          return "branch.backward";
        case Instructions.BRANCH_FALSE:
          return "branch.false";
        case Instructions.POP:
          return "pop";
        case Instructions.DUP:
          return "dup";
        case Instructions.LOAD_NULL:
          return "load.null";
        case Instructions.RETURN:
          return "return";
        case Instructions.THROW:
          return "throw";
        case Instructions.LOAD_EXCEPTION:
          return "load.exception";
        case Instructions.ADD_:
          return "c.Add";
        case Instructions.DIV_:
          return "c.Div";
        case Instructions.EQUALS_:
          return "c.Equals";
        case Instructions.LESS_THAN_:
          return "c.LessThan";
        case Instructions.EAGER_OR_:
          return "c.EagerOr";
        case Instructions.TO_BOOL_:
          return "c.ToBool";
        case Instructions.ARRAY_LENGTH_:
          return "c.ArrayLength";
        case Instructions.ARRAY_INDEX_:
          return "c.ArrayIndex";
        case Instructions.SC_OR_:
          return "sc.ScOr";
        case Instructions.INVALIDATE0:
          return "invalidate0";
        case Instructions.INVALIDATE1:
          return "invalidate1";
        case Instructions.INVALIDATE2:
          return "invalidate2";
        case Instructions.INVALIDATE3:
          return "invalidate3";
        case Instructions.INVALIDATE4:
          return "invalidate4";
      }
      throw CompilerDirectives.shouldNotReachHere("Invalid opcode");
    }

    private static boolean isInstrumentation(int opcode) {
      return false;
    }

    private static List<Argument> getArguments(
        int opcode, int bci, AbstractBytecodeNode bytecode, byte[] bytecodes, Object[] constants) {
      switch (opcode) {
        case Instructions.LOAD_ARGUMENT:
          return List.of(new IntegerArgument("index", bci + 2, bytecodes, 2));
        case Instructions.LOAD_CONSTANT:
          return List.of(new ConstantArgument("constant", bci + 2, bytecodes, constants));
        case Instructions.LOAD_LOCAL:
        case Instructions.CLEAR_LOCAL:
        case Instructions.STORE_LOCAL:
          return List.of(new LocalOffsetArgument("local_offset", bci + 2, bytecodes));
        case Instructions.BRANCH:
          return List.of(new BytecodeIndexArgument("branch_target", bci + 2, bytecodes));
        case Instructions.BRANCH_BACKWARD:
          return List.of(
              new BytecodeIndexArgument("branch_target", bci + 2, bytecodes),
              new BranchProfileArgument(
                  "loop_header_branch_profile", bci + 6, bytecode, bytecodes));
        case Instructions.BRANCH_FALSE:
        case Instructions.SC_OR_:
          return List.of(
              new BytecodeIndexArgument("branch_target", bci + 2, bytecodes),
              new BranchProfileArgument("branch_profile", bci + 6, bytecode, bytecodes));
        case Instructions.POP:
        case Instructions.DUP:
        case Instructions.LOAD_NULL:
        case Instructions.RETURN:
        case Instructions.THROW:
        case Instructions.INVALIDATE0:
          return List.of();
        case Instructions.LOAD_EXCEPTION:
          return List.of(new IntegerArgument("exception_sp", bci + 2, bytecodes, 2));
        case Instructions.ADD_:
        case Instructions.DIV_:
        case Instructions.EQUALS_:
        case Instructions.LESS_THAN_:
        case Instructions.EAGER_OR_:
        case Instructions.TO_BOOL_:
        case Instructions.ARRAY_LENGTH_:
        case Instructions.ARRAY_INDEX_:
          return List.of(new NodeProfileArgument("node", bci + 2, bytecode, bytecodes));
        case Instructions.INVALIDATE1:
          return List.of(new IntegerArgument("invalidated0", bci + 2, bytecodes, 2));
        case Instructions.INVALIDATE2:
          return List.of(
              new IntegerArgument("invalidated0", bci + 2, bytecodes, 2),
              new IntegerArgument("invalidated1", bci + 4, bytecodes, 2));
        case Instructions.INVALIDATE3:
          return List.of(
              new IntegerArgument("invalidated0", bci + 2, bytecodes, 2),
              new IntegerArgument("invalidated1", bci + 4, bytecodes, 2),
              new IntegerArgument("invalidated2", bci + 6, bytecodes, 2));
        case Instructions.INVALIDATE4:
          return List.of(
              new IntegerArgument("invalidated0", bci + 2, bytecodes, 2),
              new IntegerArgument("invalidated1", bci + 4, bytecodes, 2),
              new IntegerArgument("invalidated2", bci + 6, bytecodes, 2),
              new IntegerArgument("invalidated3", bci + 8, bytecodes, 2));
      }
      throw CompilerDirectives.shouldNotReachHere("Invalid opcode");
    }

    private abstract static sealed class AbstractArgument extends Argument
        permits LocalOffsetArgument,
            IntegerArgument,
            BytecodeIndexArgument,
            ConstantArgument,
            NodeProfileArgument,
            BranchProfileArgument {

      protected static final BytecodeDSLAccess SAFE_ACCESS =
          BytecodeDSLAccess.lookup(BytecodeRootNodesImpl.VISIBLE_TOKEN, false);
      protected static final ByteArraySupport SAFE_BYTES = SAFE_ACCESS.getByteArraySupport();

      final String name;
      final int bci;

      AbstractArgument(String name, int bci) {
        super(BytecodeRootNodesImpl.VISIBLE_TOKEN);
        this.name = name;
        this.bci = bci;
      }

      @Override
      public final String getName() {
        return name;
      }
    }

    private static final class LocalOffsetArgument extends AbstractArgument {

      final byte[] bytecodes;

      LocalOffsetArgument(String name, int bci, byte[] bytecodes) {
        super(name, bci);
        this.bytecodes = bytecodes;
      }

      @Override
      public Kind getKind() {
        return Kind.LOCAL_OFFSET;
      }

      @Override
      public int asLocalOffset() {
        byte[] bc = this.bytecodes;
        return SAFE_BYTES.getShort(bc, bci) - USER_LOCALS_START_INDEX;
      }
    }

    private static final class IntegerArgument extends AbstractArgument {

      final byte[] bytecodes;
      private final int width;

      IntegerArgument(String name, int bci, byte[] bytecodes, int width) {
        super(name, bci);
        this.bytecodes = bytecodes;
        this.width = width;
      }

      @Override
      public Kind getKind() {
        return Kind.INTEGER;
      }

      @Override
      public int asInteger() throws UnsupportedOperationException {
        byte[] bc = this.bytecodes;
        switch (width) {
          case 1:
            return SAFE_BYTES.getByte(bc, bci);
          case 2:
            return SAFE_BYTES.getShort(bc, bci);
          case 4:
            return SAFE_BYTES.getInt(bc, bci);
          default:
            throw assertionFailed("Unexpected integer width " + width);
        }
      }
    }

    private static final class BytecodeIndexArgument extends AbstractArgument {

      final byte[] bytecodes;

      BytecodeIndexArgument(String name, int bci, byte[] bytecodes) {
        super(name, bci);
        this.bytecodes = bytecodes;
      }

      @Override
      public Kind getKind() {
        return Kind.BYTECODE_INDEX;
      }

      @Override
      public int asBytecodeIndex() {
        byte[] bc = this.bytecodes;
        return SAFE_BYTES.getInt(bc, bci);
      }
    }

    private static final class ConstantArgument extends AbstractArgument {

      final byte[] bytecodes;
      final Object[] constants;

      ConstantArgument(String name, int bci, byte[] bytecodes, Object[] constants) {
        super(name, bci);
        this.bytecodes = bytecodes;
        this.constants = constants;
      }

      @Override
      public Kind getKind() {
        return Kind.CONSTANT;
      }

      @Override
      public Object asConstant() {
        byte[] bc = this.bytecodes;
        return SAFE_ACCESS.readObject(constants, SAFE_BYTES.getInt(bc, bci));
      }
    }

    private static final class NodeProfileArgument extends AbstractArgument {

      final AbstractBytecodeNode bytecode;
      final byte[] bytecodes;

      NodeProfileArgument(String name, int bci, AbstractBytecodeNode bytecode, byte[] bytecodes) {
        super(name, bci);
        this.bytecode = bytecode;
        this.bytecodes = bytecodes;
      }

      @Override
      public Kind getKind() {
        return Kind.NODE_PROFILE;
      }

      @Override
      public Node asCachedNode() {
        if (this.bytecode == null) {
          return null;
        }
        Node[] cachedNodes = this.bytecode.getCachedNodes();
        if (cachedNodes == null) {
          return null;
        }
        byte[] bc = this.bytecodes;
        return cachedNodes[SAFE_BYTES.getInt(bc, bci)];
      }
    }

    private static final class BranchProfileArgument extends AbstractArgument {

      final AbstractBytecodeNode bytecode;
      final byte[] bytecodes;

      BranchProfileArgument(String name, int bci, AbstractBytecodeNode bytecode, byte[] bytecodes) {
        super(name, bci);
        this.bytecode = bytecode;
        this.bytecodes = bytecodes;
      }

      @Override
      public Kind getKind() {
        return Kind.BRANCH_PROFILE;
      }

      @Override
      public BranchProfile asBranchProfile() {
        if (this.bytecode == null) {
          return null;
        }
        byte[] bc = this.bytecodes;
        int index = SAFE_BYTES.getInt(bc, bci);
        int[] profiles = this.bytecode.getBranchProfiles();
        if (profiles == null) {
          return new BranchProfile(index, 0, 0);
        }
        return new BranchProfile(index, profiles[index * 2], profiles[index * 2 + 1]);
      }
    }
  }

  private static final class Operations {

    private static final int BLOCK = 1;
    private static final int ROOT = 2;
    private static final int IFTHEN = 3;
    private static final int IFTHENELSE = 4;
    private static final int CONDITIONAL = 5;
    private static final int WHILE = 6;
    private static final int TRYCATCH = 7;
    private static final int TRYFINALLY = 8;
    private static final int TRYCATCHOTHERWISE = 9;
    private static final int FINALLYHANDLER = 10;
    private static final int LABEL = 11;
    private static final int BRANCH = 12;
    private static final int LOADCONSTANT = 13;
    private static final int LOADNULL = 14;
    private static final int LOADARGUMENT = 15;
    private static final int LOADEXCEPTION = 16;
    private static final int LOADLOCAL = 17;
    private static final int STORELOCAL = 18;
    private static final int RETURN = 19;
    private static final int SOURCE = 20;
    private static final int SOURCESECTIONPREFIX = 21;
    private static final int SOURCESECTIONSUFFIX = 22;
    private static final int ADD = 23;
    private static final int DIV = 24;
    private static final int EQUALS = 25;
    private static final int LESSTHAN = 26;
    private static final int EAGEROR = 27;
    private static final int TOBOOL = 28;
    private static final int ARRAYLENGTH = 29;
    private static final int ARRAYINDEX = 30;
    private static final int SCOR = 31;

    static String getName(int operation) {
      switch (operation) {
        case BLOCK:
          return "Block";
        case ROOT:
          return "Root";
        case IFTHEN:
          return "IfThen";
        case IFTHENELSE:
          return "IfThenElse";
        case CONDITIONAL:
          return "Conditional";
        case WHILE:
          return "While";
        case TRYCATCH:
          return "TryCatch";
        case TRYFINALLY:
          return "TryFinally";
        case TRYCATCHOTHERWISE:
          return "TryCatchOtherwise";
        case FINALLYHANDLER:
          return "FinallyHandler";
        case LABEL:
          return "Label";
        case BRANCH:
          return "Branch";
        case LOADCONSTANT:
          return "LoadConstant";
        case LOADNULL:
          return "LoadNull";
        case LOADARGUMENT:
          return "LoadArgument";
        case LOADEXCEPTION:
          return "LoadException";
        case LOADLOCAL:
          return "LoadLocal";
        case STORELOCAL:
          return "StoreLocal";
        case RETURN:
          return "Return";
        case SOURCE:
          return "Source";
        case SOURCESECTIONPREFIX:
          return "SourceSectionPrefix";
        case SOURCESECTIONSUFFIX:
          return "SourceSectionSuffix";
        case ADD:
          return "Add";
        case DIV:
          return "Div";
        case EQUALS:
          return "Equals";
        case LESSTHAN:
          return "LessThan";
        case EAGEROR:
          return "EagerOr";
        case TOBOOL:
          return "ToBool";
        case ARRAYLENGTH:
          return "ArrayLength";
        case ARRAYINDEX:
          return "ArrayIndex";
        case SCOR:
          return "ScOr";
      }
      throw CompilerDirectives.shouldNotReachHere("Invalid operation");
    }
  }

  private static final class ExceptionHandlerImpl extends ExceptionHandler {

    final AbstractBytecodeNode bytecode;
    final int baseIndex;

    ExceptionHandlerImpl(AbstractBytecodeNode bytecode, int baseIndex) {
      super(BytecodeRootNodesImpl.VISIBLE_TOKEN);
      this.bytecode = bytecode;
      this.baseIndex = baseIndex;
    }

    @Override
    public HandlerKind getKind() {
      return HandlerKind.CUSTOM;
    }

    @Override
    public int getStartBytecodeIndex() {
      return bytecode.handlers[baseIndex + EXCEPTION_HANDLER_OFFSET_START_BCI];
    }

    @Override
    public int getEndBytecodeIndex() {
      return bytecode.handlers[baseIndex + EXCEPTION_HANDLER_OFFSET_END_BCI];
    }

    @Override
    public int getHandlerBytecodeIndex() throws UnsupportedOperationException {
      return bytecode.handlers[baseIndex + EXCEPTION_HANDLER_OFFSET_HANDLER_BCI];
    }

    @Override
    public TagTree getTagTree() throws UnsupportedOperationException {
      return super.getTagTree();
    }
  }

  private static final class ExceptionHandlerList extends AbstractList<ExceptionHandler> {

    final AbstractBytecodeNode bytecode;

    ExceptionHandlerList(AbstractBytecodeNode bytecode) {
      this.bytecode = bytecode;
    }

    @Override
    public ExceptionHandler get(int index) {
      int baseIndex = index * EXCEPTION_HANDLER_LENGTH;
      if (baseIndex < 0 || baseIndex >= bytecode.handlers.length) {
        throw new IndexOutOfBoundsException(String.valueOf(index));
      }
      return new ExceptionHandlerImpl(bytecode, baseIndex);
    }

    @Override
    public int size() {
      return bytecode.handlers.length / EXCEPTION_HANDLER_LENGTH;
    }
  }

  private static final class SourceInformationImpl extends SourceInformation {

    final AbstractBytecodeNode bytecode;
    final int baseIndex;

    SourceInformationImpl(AbstractBytecodeNode bytecode, int baseIndex) {
      super(BytecodeRootNodesImpl.VISIBLE_TOKEN);
      this.bytecode = bytecode;
      this.baseIndex = baseIndex;
    }

    @Override
    public int getStartBytecodeIndex() {
      return bytecode.sourceInfo[baseIndex + SOURCE_INFO_OFFSET_START_BCI];
    }

    @Override
    public int getEndBytecodeIndex() {
      return bytecode.sourceInfo[baseIndex + SOURCE_INFO_OFFSET_END_BCI];
    }

    @Override
    public SourceSection getSourceSection() {
      return AbstractBytecodeNode.createSourceSection(
          bytecode.sources, bytecode.sourceInfo, baseIndex);
    }
  }

  private static final class SourceInformationList extends AbstractList<SourceInformation> {

    final AbstractBytecodeNode bytecode;

    SourceInformationList(AbstractBytecodeNode bytecode) {
      this.bytecode = bytecode;
    }

    @Override
    public SourceInformation get(int index) {
      int baseIndex = index * SOURCE_INFO_LENGTH;
      if (baseIndex < 0 || baseIndex >= bytecode.sourceInfo.length) {
        throw new IndexOutOfBoundsException(String.valueOf(index));
      }
      return new SourceInformationImpl(bytecode, baseIndex);
    }

    @Override
    public int size() {
      return bytecode.sourceInfo.length / SOURCE_INFO_LENGTH;
    }
  }

  private static final class SourceInformationTreeImpl extends SourceInformationTree {

    static final int UNAVAILABLE_ROOT = -1;

    final AbstractBytecodeNode bytecode;
    final int baseIndex;
    final List<SourceInformationTree> children;

    SourceInformationTreeImpl(AbstractBytecodeNode bytecode, int baseIndex) {
      super(BytecodeRootNodesImpl.VISIBLE_TOKEN);
      this.bytecode = bytecode;
      this.baseIndex = baseIndex;
      this.children = new LinkedList<SourceInformationTree>();
    }

    @Override
    public int getStartBytecodeIndex() {
      if (baseIndex == UNAVAILABLE_ROOT) {
        return 0;
      }
      return bytecode.sourceInfo[baseIndex + SOURCE_INFO_OFFSET_START_BCI];
    }

    @Override
    public int getEndBytecodeIndex() {
      if (baseIndex == UNAVAILABLE_ROOT) {
        return bytecode.bytecodes.length;
      }
      return bytecode.sourceInfo[baseIndex + SOURCE_INFO_OFFSET_END_BCI];
    }

    @Override
    public SourceSection getSourceSection() {
      if (baseIndex == UNAVAILABLE_ROOT) {
        return null;
      }
      return AbstractBytecodeNode.createSourceSection(
          bytecode.sources, bytecode.sourceInfo, baseIndex);
    }

    @Override
    public List<SourceInformationTree> getChildren() {
      return children;
    }

    private boolean contains(SourceInformationTreeImpl other) {
      if (baseIndex == UNAVAILABLE_ROOT) {
        return true;
      }
      return this.getStartBytecodeIndex() <= other.getStartBytecodeIndex()
          && other.getEndBytecodeIndex() <= this.getEndBytecodeIndex();
    }

    @TruffleBoundary
    private static SourceInformationTree parse(AbstractBytecodeNode bytecode) {
      int[] sourceInfo = bytecode.sourceInfo;
      if (sourceInfo.length == 0) {
        return null;
      }
      // Create a synthetic root node that contains all other SourceInformationTrees.
      SourceInformationTreeImpl root = new SourceInformationTreeImpl(bytecode, UNAVAILABLE_ROOT);
      int baseIndex = sourceInfo.length;
      SourceInformationTreeImpl current = root;
      ArrayDeque<SourceInformationTreeImpl> stack = new ArrayDeque<>();
      do {
        baseIndex -= SOURCE_INFO_LENGTH;
        SourceInformationTreeImpl newNode = new SourceInformationTreeImpl(bytecode, baseIndex);
        while (!current.contains(newNode)) {
          current = stack.pop();
        }
        current.children.addFirst(newNode);
        stack.push(current);
        current = newNode;
      } while (baseIndex > 0);
      if (root.getChildren().size() == 1) {
        // If there is an actual root source section, ignore the synthetic root we created.
        return root.getChildren().getFirst();
      } else {
        return root;
      }
    }
  }

  private static final class LocalVariableImpl extends LocalVariable {

    final AbstractBytecodeNode bytecode;
    final int baseIndex;

    LocalVariableImpl(AbstractBytecodeNode bytecode, int baseIndex) {
      super(BytecodeRootNodesImpl.VISIBLE_TOKEN);
      this.bytecode = bytecode;
      this.baseIndex = baseIndex;
    }

    @Override
    public int getStartIndex() {
      return bytecode.locals[baseIndex + LOCALS_OFFSET_START_BCI];
    }

    @Override
    public int getEndIndex() {
      return bytecode.locals[baseIndex + LOCALS_OFFSET_END_BCI];
    }

    @Override
    public Object getInfo() {
      int infoId = bytecode.locals[baseIndex + LOCALS_OFFSET_INFO];
      if (infoId == -1) {
        return null;
      } else {
        return ACCESS.readObject(bytecode.constants, infoId);
      }
    }

    @Override
    public Object getName() {
      int nameId = bytecode.locals[baseIndex + LOCALS_OFFSET_NAME];
      if (nameId == -1) {
        return null;
      } else {
        return ACCESS.readObject(bytecode.constants, nameId);
      }
    }

    @Override
    public int getLocalIndex() {
      return bytecode.locals[baseIndex + LOCALS_OFFSET_LOCAL_INDEX];
    }

    @Override
    public int getLocalOffset() {
      return bytecode.locals[baseIndex + LOCALS_OFFSET_FRAME_INDEX] - USER_LOCALS_START_INDEX;
    }

    @Override
    public FrameSlotKind getTypeProfile() {
      return null;
    }
  }

  private static final class LocalVariableList extends AbstractList<LocalVariable> {

    final AbstractBytecodeNode bytecode;

    LocalVariableList(AbstractBytecodeNode bytecode) {
      this.bytecode = bytecode;
    }

    @Override
    public LocalVariable get(int index) {
      int baseIndex = index * LOCALS_LENGTH;
      if (baseIndex < 0 || baseIndex >= bytecode.locals.length) {
        throw new IndexOutOfBoundsException(String.valueOf(index));
      }
      return new LocalVariableImpl(bytecode, baseIndex);
    }

    @Override
    public int size() {
      return bytecode.locals.length / LOCALS_LENGTH;
    }
  }

  private static class LoopCounter {

    private static final int REPORT_LOOP_STRIDE = 1 << 8;
    private static final double REPORT_LOOP_PROBABILITY = (double) 1 / (double) REPORT_LOOP_STRIDE;

    private int value;
  }

  /**
   * Debug Info:
   *
   * <pre>
   *   Specialization {@link Add#doInts}
   *     Activation probability: 1.00000
   *     With/without class size: 16/0 bytes
   * </pre>
   */
  @SuppressWarnings("javadoc")
  private static final class Add_Node extends Node {

    /**
     * State Info:
     *
     * <pre>
     *   0: SpecializationActive {@link Add#doInts}
     * </pre>
     */
    @CompilationFinal private int state_0_;

    private int execute(
        VirtualFrame frameValue, AbstractBytecodeNode $bytecode, byte[] $bc, int $bci, int $sp) {
      int state_0 = this.state_0_;
      Object child0Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 2);
      Object child1Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 1);
      if (state_0
              != 0 /* is SpecializationActive[GettingStartedBytecodeRootNode.Add.doInts(int, int)] */
          && child0Value_ instanceof Integer) {
        int child0Value__ = (int) child0Value_;
        if (child1Value_ instanceof Integer) {
          int child1Value__ = (int) child1Value_;
          return Add.doInts(child0Value__, child1Value__);
        }
      }
      CompilerDirectives.transferToInterpreterAndInvalidate();
      return executeAndSpecialize(child0Value_, child1Value_, $bytecode, $bc, $bci, $sp);
    }

    private int executeAndSpecialize(
        Object child0Value,
        Object child1Value,
        AbstractBytecodeNode $bytecode,
        byte[] $bc,
        int $bci,
        int $sp) {
      int state_0 = this.state_0_;
      if (child0Value instanceof Integer) {
        int child0Value_ = (int) child0Value;
        if (child1Value instanceof Integer) {
          int child1Value_ = (int) child1Value;
          state_0 =
              state_0
                  | 0b1 /* add SpecializationActive[GettingStartedBytecodeRootNode.Add.doInts(int, int)] */;
          this.state_0_ = state_0;
          return Add.doInts(child0Value_, child1Value_);
        }
      }
      throw new UnsupportedSpecializationException(this, null, child0Value, child1Value);
    }
  }

  /**
   * Debug Info:
   *
   * <pre>
   *   Specialization {@link Div#doInts}
   *     Activation probability: 1.00000
   *     With/without class size: 16/0 bytes
   * </pre>
   */
  @SuppressWarnings("javadoc")
  private static final class Div_Node extends Node {

    /**
     * State Info:
     *
     * <pre>
     *   0: SpecializationActive {@link Div#doInts}
     * </pre>
     */
    @CompilationFinal private int state_0_;

    private int execute(
        VirtualFrame frameValue, AbstractBytecodeNode $bytecode, byte[] $bc, int $bci, int $sp) {
      int state_0 = this.state_0_;
      Object child0Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 2);
      Object child1Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 1);
      if (state_0
              != 0 /* is SpecializationActive[GettingStartedBytecodeRootNode.Div.doInts(int, int)] */
          && child0Value_ instanceof Integer) {
        int child0Value__ = (int) child0Value_;
        if (child1Value_ instanceof Integer) {
          int child1Value__ = (int) child1Value_;
          return Div.doInts(child0Value__, child1Value__);
        }
      }
      CompilerDirectives.transferToInterpreterAndInvalidate();
      return executeAndSpecialize(child0Value_, child1Value_, $bytecode, $bc, $bci, $sp);
    }

    private int executeAndSpecialize(
        Object child0Value,
        Object child1Value,
        AbstractBytecodeNode $bytecode,
        byte[] $bc,
        int $bci,
        int $sp) {
      int state_0 = this.state_0_;
      if (child0Value instanceof Integer) {
        int child0Value_ = (int) child0Value;
        if (child1Value instanceof Integer) {
          int child1Value_ = (int) child1Value;
          state_0 =
              state_0
                  | 0b1 /* add SpecializationActive[GettingStartedBytecodeRootNode.Div.doInts(int, int)] */;
          this.state_0_ = state_0;
          return Div.doInts(child0Value_, child1Value_);
        }
      }
      throw new UnsupportedSpecializationException(this, null, child0Value, child1Value);
    }
  }

  /**
   * Debug Info:
   *
   * <pre>
   *   Specialization {@link Equals#doInts}
   *     Activation probability: 1.00000
   *     With/without class size: 16/0 bytes
   * </pre>
   */
  @SuppressWarnings("javadoc")
  private static final class Equals_Node extends Node {

    /**
     * State Info:
     *
     * <pre>
     *   0: SpecializationActive {@link Equals#doInts}
     * </pre>
     */
    @CompilationFinal private int state_0_;

    private boolean execute(
        VirtualFrame frameValue, AbstractBytecodeNode $bytecode, byte[] $bc, int $bci, int $sp) {
      int state_0 = this.state_0_;
      Object child0Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 2);
      Object child1Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 1);
      if (state_0
              != 0 /* is SpecializationActive[GettingStartedBytecodeRootNode.Equals.doInts(int, int)] */
          && child0Value_ instanceof Integer) {
        int child0Value__ = (int) child0Value_;
        if (child1Value_ instanceof Integer) {
          int child1Value__ = (int) child1Value_;
          return Equals.doInts(child0Value__, child1Value__);
        }
      }
      CompilerDirectives.transferToInterpreterAndInvalidate();
      return executeAndSpecialize(child0Value_, child1Value_, $bytecode, $bc, $bci, $sp);
    }

    private boolean executeAndSpecialize(
        Object child0Value,
        Object child1Value,
        AbstractBytecodeNode $bytecode,
        byte[] $bc,
        int $bci,
        int $sp) {
      int state_0 = this.state_0_;
      if (child0Value instanceof Integer) {
        int child0Value_ = (int) child0Value;
        if (child1Value instanceof Integer) {
          int child1Value_ = (int) child1Value;
          state_0 =
              state_0
                  | 0b1 /* add SpecializationActive[GettingStartedBytecodeRootNode.Equals.doInts(int, int)] */;
          this.state_0_ = state_0;
          return Equals.doInts(child0Value_, child1Value_);
        }
      }
      throw new UnsupportedSpecializationException(this, null, child0Value, child1Value);
    }
  }

  /**
   * Debug Info:
   *
   * <pre>
   *   Specialization {@link LessThan#doInts}
   *     Activation probability: 1.00000
   *     With/without class size: 16/0 bytes
   * </pre>
   */
  @SuppressWarnings("javadoc")
  private static final class LessThan_Node extends Node {

    /**
     * State Info:
     *
     * <pre>
     *   0: SpecializationActive {@link LessThan#doInts}
     * </pre>
     */
    @CompilationFinal private int state_0_;

    private boolean execute(
        VirtualFrame frameValue, AbstractBytecodeNode $bytecode, byte[] $bc, int $bci, int $sp) {
      int state_0 = this.state_0_;
      Object child0Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 2);
      Object child1Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 1);
      if (state_0
              != 0 /* is SpecializationActive[GettingStartedBytecodeRootNode.LessThan.doInts(int, int)] */
          && child0Value_ instanceof Integer) {
        int child0Value__ = (int) child0Value_;
        if (child1Value_ instanceof Integer) {
          int child1Value__ = (int) child1Value_;
          return LessThan.doInts(child0Value__, child1Value__);
        }
      }
      CompilerDirectives.transferToInterpreterAndInvalidate();
      return executeAndSpecialize(child0Value_, child1Value_, $bytecode, $bc, $bci, $sp);
    }

    private boolean executeAndSpecialize(
        Object child0Value,
        Object child1Value,
        AbstractBytecodeNode $bytecode,
        byte[] $bc,
        int $bci,
        int $sp) {
      int state_0 = this.state_0_;
      if (child0Value instanceof Integer) {
        int child0Value_ = (int) child0Value;
        if (child1Value instanceof Integer) {
          int child1Value_ = (int) child1Value;
          state_0 =
              state_0
                  | 0b1 /* add SpecializationActive[GettingStartedBytecodeRootNode.LessThan.doInts(int, int)] */;
          this.state_0_ = state_0;
          return LessThan.doInts(child0Value_, child1Value_);
        }
      }
      throw new UnsupportedSpecializationException(this, null, child0Value, child1Value);
    }
  }

  /**
   * Debug Info:
   *
   * <pre>
   *   Specialization {@link EagerOr#doBools}
   *     Activation probability: 1.00000
   *     With/without class size: 16/0 bytes
   * </pre>
   */
  @SuppressWarnings("javadoc")
  private static final class EagerOr_Node extends Node {

    /**
     * State Info:
     *
     * <pre>
     *   0: SpecializationActive {@link EagerOr#doBools}
     * </pre>
     */
    @CompilationFinal private int state_0_;

    private boolean execute(
        VirtualFrame frameValue, AbstractBytecodeNode $bytecode, byte[] $bc, int $bci, int $sp) {
      int state_0 = this.state_0_;
      Object child0Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 2);
      Object child1Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 1);
      if (state_0
              != 0 /* is SpecializationActive[GettingStartedBytecodeRootNode.EagerOr.doBools(boolean, boolean)] */
          && child0Value_ instanceof Boolean) {
        boolean child0Value__ = (boolean) child0Value_;
        if (child1Value_ instanceof Boolean) {
          boolean child1Value__ = (boolean) child1Value_;
          return EagerOr.doBools(child0Value__, child1Value__);
        }
      }
      CompilerDirectives.transferToInterpreterAndInvalidate();
      return executeAndSpecialize(child0Value_, child1Value_, $bytecode, $bc, $bci, $sp);
    }

    private boolean executeAndSpecialize(
        Object child0Value,
        Object child1Value,
        AbstractBytecodeNode $bytecode,
        byte[] $bc,
        int $bci,
        int $sp) {
      int state_0 = this.state_0_;
      if (child0Value instanceof Boolean) {
        boolean child0Value_ = (boolean) child0Value;
        if (child1Value instanceof Boolean) {
          boolean child1Value_ = (boolean) child1Value;
          state_0 =
              state_0
                  | 0b1 /* add SpecializationActive[GettingStartedBytecodeRootNode.EagerOr.doBools(boolean, boolean)] */;
          this.state_0_ = state_0;
          return EagerOr.doBools(child0Value_, child1Value_);
        }
      }
      throw new UnsupportedSpecializationException(this, null, child0Value, child1Value);
    }
  }

  /**
   * Debug Info:
   *
   * <pre>
   *   Specialization {@link ToBool#doBool}
   *     Activation probability: 0.65000
   *     With/without class size: 11/0 bytes
   *   Specialization {@link ToBool#doInt}
   *     Activation probability: 0.35000
   *     With/without class size: 8/0 bytes
   * </pre>
   */
  @SuppressWarnings("javadoc")
  private static final class ToBool_Node extends Node {

    /**
     * State Info:
     *
     * <pre>
     *   0: SpecializationActive {@link ToBool#doBool}
     *   1: SpecializationActive {@link ToBool#doInt}
     * </pre>
     */
    @CompilationFinal private int state_0_;

    private boolean execute(
        VirtualFrame frameValue, AbstractBytecodeNode $bytecode, byte[] $bc, int $bci, int $sp) {
      int state_0 = this.state_0_;
      Object child0Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 1);
      if (state_0
          != 0 /* is SpecializationActive[GettingStartedBytecodeRootNode.ToBool.doBool(boolean)] || SpecializationActive[GettingStartedBytecodeRootNode.ToBool.doInt(int)] */) {
        if ((state_0 & 0b1)
                != 0 /* is SpecializationActive[GettingStartedBytecodeRootNode.ToBool.doBool(boolean)] */
            && child0Value_ instanceof Boolean) {
          boolean child0Value__ = (boolean) child0Value_;
          return ToBool.doBool(child0Value__);
        }
        if ((state_0 & 0b10)
                != 0 /* is SpecializationActive[GettingStartedBytecodeRootNode.ToBool.doInt(int)] */
            && child0Value_ instanceof Integer) {
          int child0Value__ = (int) child0Value_;
          return ToBool.doInt(child0Value__);
        }
      }
      CompilerDirectives.transferToInterpreterAndInvalidate();
      return executeAndSpecialize(child0Value_, $bytecode, $bc, $bci, $sp);
    }

    private boolean executeAndSpecialize(
        Object child0Value, AbstractBytecodeNode $bytecode, byte[] $bc, int $bci, int $sp) {
      int state_0 = this.state_0_;
      if (child0Value instanceof Boolean) {
        boolean child0Value_ = (boolean) child0Value;
        state_0 =
            state_0
                | 0b1 /* add SpecializationActive[GettingStartedBytecodeRootNode.ToBool.doBool(boolean)] */;
        this.state_0_ = state_0;
        return ToBool.doBool(child0Value_);
      }
      if (child0Value instanceof Integer) {
        int child0Value_ = (int) child0Value;
        state_0 =
            state_0
                | 0b10 /* add SpecializationActive[GettingStartedBytecodeRootNode.ToBool.doInt(int)] */;
        this.state_0_ = state_0;
        return ToBool.doInt(child0Value_);
      }
      throw new UnsupportedSpecializationException(this, null, child0Value);
    }
  }

  /**
   * Debug Info:
   *
   * <pre>
   *   Specialization {@link ArrayLength#doInt}
   *     Activation probability: 1.00000
   *     With/without class size: 16/0 bytes
   * </pre>
   */
  @SuppressWarnings("javadoc")
  private static final class ArrayLength_Node extends Node {

    /**
     * State Info:
     *
     * <pre>
     *   0: SpecializationActive {@link ArrayLength#doInt}
     * </pre>
     */
    @CompilationFinal private int state_0_;

    private int execute(
        VirtualFrame frameValue, AbstractBytecodeNode $bytecode, byte[] $bc, int $bci, int $sp) {
      int state_0 = this.state_0_;
      Object child0Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 1);
      if (state_0
              != 0 /* is SpecializationActive[GettingStartedBytecodeRootNode.ArrayLength.doInt(int[])] */
          && child0Value_ instanceof int[]) {
        int[] child0Value__ = (int[]) child0Value_;
        return ArrayLength.doInt(child0Value__);
      }
      CompilerDirectives.transferToInterpreterAndInvalidate();
      return executeAndSpecialize(child0Value_, $bytecode, $bc, $bci, $sp);
    }

    private int executeAndSpecialize(
        Object child0Value, AbstractBytecodeNode $bytecode, byte[] $bc, int $bci, int $sp) {
      int state_0 = this.state_0_;
      if (child0Value instanceof int[]) {
        int[] child0Value_ = (int[]) child0Value;
        state_0 =
            state_0
                | 0b1 /* add SpecializationActive[GettingStartedBytecodeRootNode.ArrayLength.doInt(int[])] */;
        this.state_0_ = state_0;
        return ArrayLength.doInt(child0Value_);
      }
      throw new UnsupportedSpecializationException(this, null, child0Value);
    }
  }

  /**
   * Debug Info:
   *
   * <pre>
   *   Specialization {@link ArrayIndex#doInt}
   *     Activation probability: 1.00000
   *     With/without class size: 16/0 bytes
   * </pre>
   */
  @SuppressWarnings("javadoc")
  private static final class ArrayIndex_Node extends Node {

    /**
     * State Info:
     *
     * <pre>
     *   0: SpecializationActive {@link ArrayIndex#doInt}
     * </pre>
     */
    @CompilationFinal private int state_0_;

    private int execute(
        VirtualFrame frameValue, AbstractBytecodeNode $bytecode, byte[] $bc, int $bci, int $sp) {
      int state_0 = this.state_0_;
      Object child0Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 2);
      Object child1Value_ = FRAMES.uncheckedGetObject(frameValue, $sp - 1);
      if (state_0
              != 0 /* is SpecializationActive[GettingStartedBytecodeRootNode.ArrayIndex.doInt(int[], int)] */
          && child0Value_ instanceof int[]) {
        int[] child0Value__ = (int[]) child0Value_;
        if (child1Value_ instanceof Integer) {
          int child1Value__ = (int) child1Value_;
          return ArrayIndex.doInt(child0Value__, child1Value__);
        }
      }
      CompilerDirectives.transferToInterpreterAndInvalidate();
      return executeAndSpecialize(child0Value_, child1Value_, $bytecode, $bc, $bci, $sp);
    }

    private int executeAndSpecialize(
        Object child0Value,
        Object child1Value,
        AbstractBytecodeNode $bytecode,
        byte[] $bc,
        int $bci,
        int $sp) {
      int state_0 = this.state_0_;
      if (child0Value instanceof int[]) {
        int[] child0Value_ = (int[]) child0Value;
        if (child1Value instanceof Integer) {
          int child1Value_ = (int) child1Value;
          state_0 =
              state_0
                  | 0b1 /* add SpecializationActive[GettingStartedBytecodeRootNode.ArrayIndex.doInt(int[], int)] */;
          this.state_0_ = state_0;
          return ArrayIndex.doInt(child0Value_, child1Value_);
        }
      }
      throw new UnsupportedSpecializationException(this, null, child0Value, child1Value);
    }
  }
}
