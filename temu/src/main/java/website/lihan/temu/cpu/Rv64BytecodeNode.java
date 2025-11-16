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
import website.lihan.temu.Rv64Context;
import website.lihan.temu.cpu.RvUtils.IInstruct;
import website.lihan.temu.cpu.RvUtils.UInstruct;
import website.lihan.temu.cpu.instr.Amo;
import website.lihan.temu.cpu.instr.BranchNode;
import website.lihan.temu.cpu.instr.JalNode;
import website.lihan.temu.cpu.instr.JalrNodeGen;
import website.lihan.temu.cpu.instr.LoadNode;
import website.lihan.temu.cpu.instr.Op;
import website.lihan.temu.cpu.instr.Op32;
import website.lihan.temu.cpu.instr.OpImm;
import website.lihan.temu.cpu.instr.OpImm32;
import website.lihan.temu.cpu.instr.StoreNode;
import website.lihan.temu.cpu.instr.SystemOp;
import website.lihan.temu.device.Bus;

public class Rv64BytecodeNode extends Node implements BytecodeOSRNode {
  final long baseAddr;
  final int entryBci;

  final Rv64Context context;
  final Bus bus;

  @CompilationFinal Rv64State cpu;

  @CompilationFinal(dimensions = 1)
  byte[] bc;

  @CompilationFinal(dimensions = 1)
  Node[] nodes;

  @CompilationFinal private Object osrMetadata;

  private static final ByteArraySupport BYTES = ByteArraySupport.littleEndian();

  public Rv64BytecodeNode(long baseAddr, byte[] bc, Node[] nodes, int entryBci, Rv64State cpu) {
    assert baseAddr <= entryBci && entryBci < baseAddr + bc.length;
    this.baseAddr = baseAddr;
    this.bc = bc;
    this.nodes = nodes;
    this.entryBci = entryBci;
    this.cpu = cpu;

    this.context = Rv64Context.get(this);
    this.bus = context.getBus();
  }

  public Object execute(VirtualFrame frame) {
    return executeFromBci(frame, this.entryBci);
  }

  @Override
  public Object executeOSR(VirtualFrame osrFrame, int target, Object interpreterState) {
    return executeFromBci(osrFrame, target);
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
  public Object executeFromBci(VirtualFrame frame, int startBci) {
    int bci = startBci;
    while (true) {
      CompilerAsserts.partialEvaluationConstant(bci);
      if (bci + 4 > bc.length) {
        // reached the end of the bytecode page
        throw JumpException.create(getPc(bci));
      }
      int instr = BYTES.getInt(bc, bci);
      CompilerAsserts.partialEvaluationConstant(instr);

      // website.lihan.temu.Utils.printf("pc=%08x: %08x, sp=%08x, ra=%08x\n", baseAddr + bci, instr,
      // cpu.getReg(2), cpu.getReg(1));
      int nextBci = bci + 4;

      int opcode = instr & 0x7f;
      switch (opcode) {
        case Opcodes.OP_IMM -> OpImm.execute(cpu, getPc(bci), instr);
        case Opcodes.OP -> Op.execute(cpu, instr);
        case Opcodes.OP_IMM_32 -> OpImm32.execute(cpu, getPc(bci), instr);
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
          final var node = JalNode.class.cast(nodes[instr >> 8]);
          if (node.isCall) {
            // This is likely a function call from the ABI convention.
            // If the following ret instruction (jalr zero, 0(ra)) is jumping back to
            // the caller (ra == current_pc + 4), we can optimize out the JumpException
            // and simply return to the caller directly.
            // Utils.printf("Calling from %08x to %08x\n", getPc(bci), node.targetPc);
            node.call(cpu);
          } else {
            final var targetPc = node.targetPc;
            if (targetPc < baseAddr || targetPc >= baseAddr + bc.length) {
              throw JumpException.create(targetPc);
            }
            // ExplodeLoop phrase during partial evaluation happens before inlining function calls.
            // To ensure nextBci is a constant, we cannot encapsulation the jump logic into a
            // method.
            nextBci = (int) (node.targetPc - baseAddr);
            cpu.setReg(node.rd, node.returnPc);
          }
        }
        case Opcodes.JALR -> {
          final var node = JalrNodeGen.class.cast(nodes[instr >> 8]);
          // An optimization in the JalNode above takes an assumption that most jalr instructions
          // are function returns (jalr zero, 0(ra)).
          // And if target address equals to the return address of previous `jal ra, xx`, we can
          // optimize out the JumpException throwing and just return to the caller directly.
          final var nextPc = cpu.getReg(node.rs1) + node.imm;
          if (node.rd == 1) {
            // Utils.printf("Indirect call from %08x to %08x\n", getPc(bci), nextPc);
            node.execute(cpu);
          } else if (node.rd == 0 && JalNode.jalrIsReturn(frame, nextPc)) {
            // Utils.printf("Returning from %08x to %08x\n", getPc(bci), nextPc);
            return 0;
          } else {
            // Utils.printf("Indirect jump from %08x to %08x\n", getPc(bci), nextPc);
            cpu.setReg(node.rd, node.returnPc);
            // nextBci must be a constant during explode looping. But the targetPc of jalr
            // instruction
            // comes from a register and is not a constant.
            // Hence, we have to throw a JumpException here to exit the explode looping.
            throw JumpException.create(nextPc);
          }
        }
        case Opcodes.BRANCH -> {
          final var branchNode = BranchNode.class.cast(nodes[instr >> 8]);
          final var cond =
              branchNode.condition(cpu.getReg(branchNode.rs1), cpu.getReg(branchNode.rs2));
          if (cond) {
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
        case Opcodes.SYSTEM -> SystemOp.execute(context, cpu, getPc(bci), instr, nodes);
        case Opcodes.MEM_MISC -> {
          final var i = IInstruct.decode(instr);
          switch (i.funct3()) {
            case 0b000 -> {
              // FENCE
            }
            case 0b001 -> {
              context.execPageCache.clear();
              throw JumpException.create(getPc(bci) + 4);
            }
            default ->
                throw IllegalInstructionException.create(
                    getPc(bci), "Unknown funct3 %d of MEM_MISC", i.funct3());
          }
        }
        case Opcodes.AMO -> {
          var node = Amo.class.cast(nodes[instr >> 8]);
          node.execute(cpu);
        }
        default ->
            throw IllegalInstructionException.create(getPc(bci), "Unknown opcode %x", opcode);
      }

      if (CompilerDirectives.inInterpreter() && nextBci < bci) { // back-edge
        // cpu.throwPendingInterrupt(getPc(bci));
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

  private void doLoad(Rv64State cpu, int bci, int instr) {
    var loadNode = LoadNode.class.cast(nodes[instr >> 8]);
    loadNode.throwIfInvalid();
    var addr = cpu.getReg(loadNode.rs1) + loadNode.offset;
    var value = loadNode.execute(cpu, addr);
    cpu.setReg(loadNode.rd, value);
  }

  private void doStore(Rv64State cpu, int bci, int instr) {
    var storeNode = StoreNode.class.cast(nodes[instr >> 8]);
    storeNode.throwIfInvalid();
    var addr = cpu.getReg(storeNode.rs1) + storeNode.offset;
    storeNode.execute(cpu, addr, cpu.getReg(storeNode.rs2));
  }
}
