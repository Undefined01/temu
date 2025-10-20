package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.HostCompilerDirectives.BytecodeInterpreterSwitch;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.memory.ByteArraySupport;
import com.oracle.truffle.api.nodes.BytecodeOSRNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import java.util.ArrayList;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.RvUtils.BInstruct;
import website.lihan.temu.cpu.RvUtils.IInstruct;
import website.lihan.temu.cpu.RvUtils.JInstruct;
import website.lihan.temu.cpu.RvUtils.SInstruct;
import website.lihan.temu.cpu.RvUtils.UInstruct;
import website.lihan.temu.cpu.instr.BranchNode;
import website.lihan.temu.cpu.instr.CallNode;
import website.lihan.temu.cpu.instr.CsrRWNode;
import website.lihan.temu.cpu.instr.JalNode;
import website.lihan.temu.cpu.instr.LoadNode;
import website.lihan.temu.cpu.instr.LoadNodeGen;
import website.lihan.temu.cpu.instr.Op;
import website.lihan.temu.cpu.instr.Op32;
import website.lihan.temu.cpu.instr.OpImm;
import website.lihan.temu.cpu.instr.OpImm32;
import website.lihan.temu.cpu.instr.StoreNode;
import website.lihan.temu.cpu.instr.StoreNodeGen;
import website.lihan.temu.cpu.instr.SystemOp;
import website.lihan.temu.device.Bus;
import website.lihan.temu.device.RTC;
import website.lihan.temu.cpu.Opcodes.SystemFunct3;

public class Rv64BytecodeNode extends Node implements BytecodeOSRNode {
  @CompilationFinal long baseAddr;
  @CompilationFinal int entryBci;
  @Child Bus bus;

  @CompilationFinal(dimensions = 1)
  private byte[] bc;

  @CompilationFinal(dimensions = 1)
  private Node[] nodes;

  @CompilationFinal private Object osrMetadata;

  private static final ByteArraySupport BYTES = ByteArraySupport.littleEndian();

  public Rv64BytecodeNode(long baseAddr, byte[] bc, int entryBci) {
    assert baseAddr <= entryBci && entryBci < baseAddr + bc.length;
    this.baseAddr = baseAddr;
    this.bc = bc;
    this.entryBci = entryBci;

    var context = Rv64Context.get(this);
    this.bus = context.getBus();
    this.nodes = null;
  }

  public Object execute(VirtualFrame frame, Rv64State cpu) {
    if (nodes == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      var context = Rv64Context.get(this);
      var execCache = context.getExecPageCache();
      var cachedNodes = execCache.getCachedNodes(baseAddr);
      if (cachedNodes == null) {
        cachedNodes = createCachedNodes();
        execCache.putCachedNodes(baseAddr, cachedNodes);
      }
      this.nodes = cachedNodes;
    }
    return executeFromBci(frame, cpu, this.entryBci);
  }

  @Override
  public Object executeOSR(VirtualFrame osrFrame, int target, Object interpreterState) {
    return executeFromBci(osrFrame, (Rv64State) interpreterState, target);
  }

  @Override
  public Object getOSRMetadata() {
    return osrMetadata;
  }

  @Override
  public void setOSRMetadata(Object osrMetadata) {
    this.osrMetadata = osrMetadata;
  }

  @BytecodeInterpreterSwitch
  @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.MERGE_EXPLODE)
  public Object executeFromBci(VirtualFrame frame, Rv64State cpu, int startBci) {
    int bci = startBci;
    while (true) {
      CompilerAsserts.partialEvaluationConstant(bci);
      int instr = BYTES.getInt(bc, bci);
      CompilerAsserts.partialEvaluationConstant(instr);

      cpu.pc = getPc(bci);
      // Utils.printf("pc=%08x: %08x, sp=%08x\n", cpu.pc, instr, cpu.getReg(2));
      int nextBci = bci + 4;

      if (cpu.isInterruptEnabled() && RTC.checkInterrupt()) {
        throw InterruptException.create(cpu.pc, InterruptException.Cause.STIMER);
      }

      int opcode = instr & 0x7f;
      switch (opcode) {
        case Opcodes.OP_IMM -> OpImm.execute(cpu, instr);
        case Opcodes.OP -> Op.execute(cpu, instr);
        case Opcodes.OP_IMM_32 -> OpImm32.execute(cpu, instr);
        case Opcodes.OP_32 -> Op32.execute(cpu, instr);
        case Opcodes.LUI -> {
          final var u = UInstruct.decode(instr);
          cpu.setReg(u.rd(), u.imm());
        }
        case Opcodes.AUIPC -> {
          final var u = UInstruct.decode(instr);
          cpu.setReg(u.rd(), getPc(bci) + u.imm());
        }
        case Opcodes.JAL -> {
          final var isCall = (instr & 0x80) != 0;
          final var nodeIdx = (instr >> 8);
          if (isCall) {
            // This is likely a function call from the ABI convention.
            // If the following ret instruction (jalr zero, 0(ra)) is jumping back to
            // the caller (ra == current_pc + 4), we can optimize out the JumpException
            // and simply return to the caller directly.
            CallNode.class.cast(nodes[nodeIdx]).call(cpu);
          } else {
            final var jalNode = JalNode.class.cast(nodes[nodeIdx]);
            final var targetPc = jalNode.targetPc;
            if (targetPc < baseAddr || targetPc >= baseAddr + bc.length) {
              throw JumpException.create(targetPc);
            }
            // ExplodeLoop phrase during partial evaluation happens before inlining function calls.
            // To ensure nextBci is a constant, we cannot encapsulation the jump logic into a
            // method.
            nextBci = (int) (jalNode.targetPc - baseAddr);
            cpu.setReg(jalNode.rd, jalNode.returnPc);
          }
        }
        case Opcodes.JALR -> {
          final var i = IInstruct.decode(instr);
          // An optimization in the JalNode above takes an assumption that most jalr instructions
          // are function returns (jalr zero, 0(ra)).
          // And if target address equals to the return address of previous `jal ra, xx`, we can
          // optimize out the JumpException throwing and just return to the caller directly.
          final var nextPc = cpu.getReg(i.rs1()) + i.imm();
          if (CallNode.jalrIsReturn(frame, nextPc)) {
            return 0;
          }
          cpu.setReg(i.rd(), getPc(bci + 4));
          // nextBci must be a constant during explode looping. But the targetPc of jalr instruction
          // comes from a register and is not a constant.
          // Hence, we have to throw a JumpException here to exit the explode looping.
          throw JumpException.create(nextPc);
        }
        case Opcodes.BRANCH -> {
          final var branchNode = BranchNode.class.cast(nodes[(instr >> 8)]);
          final var cond =
              branchNode.condition(cpu.getReg(branchNode.rs1), cpu.getReg(branchNode.rs2));
          if (branchNode.profileBranch(cond)) {
            final var targetPc = branchNode.targetPc;
            if (targetPc < baseAddr || targetPc >= baseAddr + bc.length) {
              throw JumpException.create(targetPc);
            }
            // ExplodeLoop phrase during partial evaluation happens before inlining function calls.
            // To ensure nextBci is a constant, we cannot encapsulation the jump logic into a
            // method.
            nextBci = (int) (targetPc - baseAddr);
          }
        }
        case Opcodes.LOAD -> doLoad(cpu, bci, instr);
        case Opcodes.STORE -> doStore(cpu, bci, instr);
        case Opcodes.SYSTEM -> SystemOp.execute(cpu, instr, nodes);
        case Opcodes.FENCE -> {
          // No operation
        }
        default -> throw IllegalInstructionException.create(getPc(bci), instr);
      }

      if (CompilerDirectives.inInterpreter() && nextBci < bci) { // back-edge
        if (BytecodeOSRNode.pollOSRBackEdge(this, 1)) { // OSR can be tried
          Object result = BytecodeOSRNode.tryOSR(this, nextBci, cpu, null, frame);
          if (result != null) { // OSR was performed
            return result;
          }
        }
      }
      bci = nextBci;
    }
  }

  private long getPc(int bci) {
    return baseAddr + (long) bci;
  }

  public Node[] createCachedNodes() {
    var nodeList = new ArrayList<Node>();
    for (int bci = 0; bci + 4 <= bc.length; bci += 4) {
      final var pc = baseAddr + bci;
      var instr = BYTES.getInt(bc, bci);
      final var opcode = instr & 0x7f;
      switch (opcode) {
        case Opcodes.JAL -> {
          final var j = JInstruct.decode(instr);
          final var targetPc = pc + j.imm();
          final var returnPc = pc + 4;
          final var isCall = j.rd() == 1;
          final var nodeIdx = nodeList.size();
          if (isCall) {
            // likely a function call
            nodeList.add(new CallNode(targetPc, returnPc));
          } else {
            nodeList.add(new JalNode(targetPc, returnPc, j.rd()));
          }
          instr = Opcodes.JAL | (isCall ? 0x80 : 0) | (nodeIdx << 8);
          BYTES.putInt(bc, bci, instr);
        }
        case Opcodes.BRANCH -> {
          final var b = BInstruct.decode(instr);
          final var targetPc = pc + b.imm();
          final var nodeIdx = nodeList.size();
          nodeList.add(new BranchNode(b.funct3(), b.rs1(), b.rs2(), targetPc));
          instr = Opcodes.BRANCH | (nodeIdx << 8);
          BYTES.putInt(bc, bci, instr);
        }
        case Opcodes.LOAD -> {
          final var i = IInstruct.decode(instr);
          final int length =
              switch (i.funct3() & 0b11) {
                case 0b00 -> 1;
                case 0b01 -> 2;
                case 0b10 -> 4;
                case 0b11 -> 8;
                default -> 0;
              };
          final var isValidInstr = length != 0;
          final var shouldSignExtend = (i.funct3() & 0b100) == 0;

          final var nodeIdx = nodeList.size();
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
        case Opcodes.SYSTEM -> {
          final var i = IInstruct.decode(instr);
          switch (i.funct3()) {
            case SystemFunct3.CSRRW, SystemFunct3.CSRRS, SystemFunct3.CSRRC,
                SystemFunct3.CSRRWI, SystemFunct3.CSRRSI, SystemFunct3.CSRRCI -> {
              final var nodeIdx = nodeList.size();
              nodeList.add(new CsrRWNode(i.imm() & 0xfff, i.rs1(), i.rd(), i.funct3()));
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

  private void doLoad(Rv64State cpu, int bci, int instr) {
    var loadNode = LoadNode.class.cast(nodes[(instr >> 8)]);
    var isValidInstr = (instr & 0x80) != 0;
    if (!isValidInstr) {
      throw IllegalInstructionException.create(getPc(bci), instr);
    }
    var addr = cpu.getReg(loadNode.rs1) + loadNode.offset;
    var value = loadNode.execute(addr);

    cpu.setReg(loadNode.rd, value);
  }

  private void doStore(Rv64State cpu, int bci, int instr) {
    var storeNode = StoreNode.class.cast(nodes[(instr >> 8)]);
    var isValidInstr = (instr & 0x80) != 0;
    if (!isValidInstr) {
      throw IllegalInstructionException.create(getPc(bci), instr);
    }
    var addr = cpu.getReg(storeNode.rs1) + storeNode.offset;
    storeNode.execute(addr, cpu.getReg(storeNode.rs2));
  }
}
