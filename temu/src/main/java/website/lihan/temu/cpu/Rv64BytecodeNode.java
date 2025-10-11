package website.lihan.temu.cpu;

import static website.lihan.temu.cpu.Utils.BInstruct;
import static website.lihan.temu.cpu.Utils.IInstruct;
import static website.lihan.temu.cpu.Utils.RInstruct;
import static website.lihan.temu.cpu.Utils.UInstruct;
import static website.lihan.temu.cpu.Utils.signExtend;
import static website.lihan.temu.cpu.Utils.signExtend32;

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
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.Utils.BInstruct;
import website.lihan.temu.cpu.Utils.IInstruct;
import website.lihan.temu.cpu.Utils.RInstruct;
import website.lihan.temu.cpu.Utils.UInstruct;
import website.lihan.temu.device.Bus;

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
      this.nodes = context.getExecPageCache().getCachedNodes(baseAddr, bc);
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

      int opcode = instr & 0x7f;
      switch (opcode) {
        case Opcodes.OP_IMM -> {
          var i = IInstruct.decode(instr);
          var res = calcArthimeticImm(i.funct3(), i.funct7(), cpu.getReg(i.rs1()), i.imm());
          cpu.setReg(i.rd(), res);
        }
        case Opcodes.OP -> {
          var r = RInstruct.decode(instr);
          var res =
              calcArthimetic(r.funct3(), r.funct7(), cpu.getReg(r.rs1()), cpu.getReg(r.rs2()));
          cpu.setReg(r.rd(), res);
        }
        case Opcodes.OP_IMM_32 -> {
          var i = IInstruct.decode(instr);
          long res =
              calcArthimeticImm32(i.funct3(), i.funct7(), (int) cpu.getReg(i.rs1()), i.imm());
          res = signExtend(res, 32);
          cpu.setReg(i.rd(), res);
        }
        case Opcodes.OP_32 -> {
          var r = RInstruct.decode(instr);
          long res =
              calcArthimetic32(
                  r.funct3(), r.funct7(), (int) cpu.getReg(r.rs1()), (int) cpu.getReg(r.rs2()));
          res = signExtend(res, 32);
          cpu.setReg(r.rd(), res);
        }
        case Opcodes.LUI -> {
          final var u = UInstruct.decode(instr);
          cpu.setReg(u.rd(), u.imm());
        }
        case Opcodes.AUIPC -> {
          final var u = UInstruct.decode(instr);
          cpu.setReg(u.rd(), getPc(bci) + u.imm());
        }
        case Opcodes.JAL -> {
          var isCall = (instr & 0x80) != 0;
          var nodeIdx = (instr >> 8);
          if (isCall) {
            CallNode.class.cast(nodes[nodeIdx]).call(cpu);
          } else {
            var jalNode = JalNode.class.cast(nodes[nodeIdx]);
            nextBci = (int) (jalNode.targetPc - baseAddr);
            cpu.setReg(jalNode.rd, jalNode.returnPc);
          }
        }
        case Opcodes.JALR -> {
          final var i = IInstruct.decode(instr);
          final var imm = (instr >> 20);
          final var nextPc = cpu.getReg(i.rs1()) + (long) imm;
          if (frame.getArguments().length > 2 && (long) frame.getArguments()[1] == nextPc) {
            return 0;
          }
          cpu.setReg(i.rd(), getPc(bci + 4));
          nextBci = (int) (nextPc - baseAddr);
          throw JumpException.create(nextBci + baseAddr);
        }
        case Opcodes.BRANCH -> {
          final var b = BInstruct.decode(instr);
          var cond = calcComparsion(b.funct3(), cpu.getReg(b.rs1()), cpu.getReg(b.rs2()));
          if (cond) {
            var imm12 = (instr >> 31) << 12;
            var imm11 = ((instr >> 7) & 0x1) << 11;
            var imm10_5 = ((instr >> 25) & 0x3f) << 5;
            var imm4_1 = ((instr >> 8) & 0xf) << 1;
            var imm = signExtend32(imm12 | imm11 | imm10_5 | imm4_1, 13);
            nextBci = bci + imm;
          }
        }
        case Opcodes.LOAD -> doLoad(cpu, bci, instr);
        case Opcodes.STORE -> doStore(cpu, bci, instr);
        case Opcodes.SYSTEM -> {
          final var i = IInstruct.decode(instr);
          switch (i.funct3()) {
            case 0b000 -> {
              switch (i.imm()) {
                case 0b000000000000 -> {
                  // ECALL
                  throw InterruptException.create(getPc(bci), 9);
                }
                case 0b000000000001 -> {
                  // EBREAK
                  throw HaltException.create(getPc(bci), cpu.getReg(10));
                }
                case 0b000100000010 -> {
                  // SRET
                  var sstatus = cpu.readCSR(Rv64State.CSR.SSTATUS);
                  var sepc = cpu.readCSR(Rv64State.CSR.SEPC);
                  var s = (sstatus >> 8) & 1; // SPP
                  sstatus = (sstatus & ~(1 << 1)) | ((sstatus & (1 << 5)) >> 4); // SPIE -> SIE
                  sstatus &= ~(1 << 5); // SPIE = 0
                  if (s == 1) {
                    sstatus |= 1 << 3; // SIE = 1
                  } else {
                    sstatus &= ~(1 << 3); // SIE = 0
                  }
                  cpu.writeCSR(Rv64State.CSR.SSTATUS, sstatus);
                  throw JumpException.create(sepc);
                }
                default -> throw IllegalInstructionException.create(getPc(bci), instr);
              }
            }
            case 0b001 -> {
              // CSRRW
              var oldRegValue = cpu.getReg(i.rs1());
              var oldCSRValue = cpu.readCSR(i.imm());
              cpu.setReg(i.rd(), oldCSRValue);
              cpu.writeCSR(i.imm(), oldRegValue);
            }
            case 0b010 -> {
              // CSRRS
              var oldRegValue = cpu.getReg(i.rs1());
              var oldCSRValue = cpu.readCSR(i.imm());
              cpu.setReg(i.rd(), oldCSRValue);
              cpu.writeCSR(i.imm(), oldCSRValue | oldRegValue);
            }
            case 0b011 -> {
              // CSRRC
              var oldRegValue = cpu.getReg(i.rs1());
              var oldCSRValue = cpu.readCSR(i.imm());
              cpu.setReg(i.rd(), oldCSRValue);
              cpu.writeCSR(i.imm(), oldCSRValue & ~oldRegValue);
            }
            case 0b101 -> {
              // CSRRWI
              var oldCSRValue = cpu.readCSR(i.imm());
              cpu.setReg(i.rd(), oldCSRValue);
              cpu.writeCSR(i.imm(), i.rs1());
            }
            case 0b110 -> {
              // CSRRSI
              var oldCSRValue = cpu.readCSR(i.imm());
              cpu.setReg(i.rd(), oldCSRValue);
              cpu.writeCSR(i.imm(), oldCSRValue | i.rs1());
            }
            case 0b111 -> {
              // CSRRCI
              var oldCSRValue = cpu.readCSR(i.imm());
              cpu.setReg(i.rd(), oldCSRValue);
              cpu.writeCSR(i.imm(), oldCSRValue & ~i.rs1());
            }
            default -> throw IllegalInstructionException.create(getPc(bci), instr);
          }
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

  private long calcArthimetic(int funct3, int funct7, long op1, long op2) {
    var shamt = op2 & 0x3f;
    return switch (funct3) {
      case 0b000 ->
          switch (funct7) {
            case 0b0000000 -> op1 + op2;
            case 0b0100000 -> op1 - op2;
            case 0b0000001 -> op1 * op2;
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b001 ->
          switch (funct7) {
            case 0b0000000 -> op1 << shamt;
            case 0b0000001 -> Math.multiplyHigh(op1, op2);
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b010 ->
          switch (funct7) {
            case 0b0000000 -> op1 < op2 ? 1 : 0;
            case 0b0000001 -> Utils.signedUnsignedMultiplyHigh(op1, op2);
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b011 ->
          switch (funct7) {
            case 0b0000000 -> Long.compareUnsigned(op1, op2) < 0 ? 1 : 0;
            case 0b0000001 -> Math.unsignedMultiplyHigh(op1, op2);
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b100 ->
          switch (funct7) {
            case 0b0000000 -> op1 ^ op2;
            case 0b0000001 -> {
              if (op2 == 0) {
                // Division by zero
                yield -1;
              }
              if (op1 == Long.MIN_VALUE && op2 == -1) {
                // Overflow
                yield op1;
              }
              yield op1 / op2;
            }
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b101 ->
          switch (funct7) {
            case 0b0000000 -> op1 >>> shamt;
            case 0b0100000 -> op1 >> shamt;
            case 0b0000001 -> op2 != 0 ? Long.divideUnsigned(op1, op2) : -1;
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b110 ->
          switch (funct7) {
            case 0b0000000 -> op1 | op2;
            case 0b0000001 -> {
              if (op2 == 0) {
                // Division by zero
                yield op1;
              }
              if (op1 == Long.MIN_VALUE && op2 == -1) {
                // Overflow
                yield 0;
              }
              yield op1 % op2;
            }
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b111 ->
          switch (funct7) {
            case 0b0000000 -> op1 & op2;
            case 0b0000001 -> op2 != 0 ? Long.remainderUnsigned(op1, op2) : op1;
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      default -> throw IllegalInstructionException.create("Invalid funct3 %d", funct3);
    };
  }

  private long calcArthimeticImm(int funct3, int funct7, long op1, long op2) {
    var shamt = op2 & 0x3f;
    return switch (funct3) {
      case 0b000 -> op1 + op2;
      case 0b001 -> op1 << shamt;
      case 0b010 -> op1 < op2 ? 1 : 0;
      case 0b011 -> Long.compareUnsigned(op1, op2) < 0 ? 1 : 0;
      case 0b100 -> op1 ^ op2;
      case 0b101 ->
          switch (funct7 >> 1) {
            case 0b000000 -> op1 >>> shamt;
            case 0b010000 -> op1 >> shamt;
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b110 -> op1 | op2;
      case 0b111 -> op1 & op2;
      default -> throw IllegalInstructionException.create("Invalid funct3 %d", funct3);
    };
  }

  private int calcArthimetic32(int funct3, int funct7, int op1, int op2) {
    var shamt = op2 & 0x1f;
    return switch (funct3) {
      case 0b000 ->
          switch (funct7) {
            case 0b0000000 -> op1 + op2;
            case 0b0100000 -> op1 - op2;
            case 0b0000001 -> op1 * op2;
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b001 ->
          switch (funct7) {
            case 0b0000000 -> op1 << shamt;
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b100 ->
          switch (funct7) {
            case 0b0000001 -> {
              if (op2 == 0) {
                // Division by zero
                yield -1;
              }
              if (op1 == Integer.MIN_VALUE && op2 == -1) {
                // Overflow
                yield op1;
              }
              yield op1 / op2;
            }
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b101 ->
          switch (funct7) {
            case 0b0000000 -> op1 >>> shamt;
            case 0b0100000 -> op1 >> shamt;
            case 0b0000001 -> op2 != 0 ? Integer.divideUnsigned(op1, op2) : -1;
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b110 ->
          switch (funct7) {
            case 0b0000001 -> {
              if (op2 == 0) {
                // Division by zero
                yield op1;
              }
              if (op1 == Integer.MIN_VALUE && op2 == -1) {
                // Overflow
                yield 0;
              }
              yield op1 % op2;
            }
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b111 ->
          switch (funct7) {
            case 0b0000001 -> op2 != 0 ? Integer.remainderUnsigned(op1, op2) : op1;
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      default -> throw IllegalInstructionException.create("Invalid funct3 %d", funct3);
    };
  }

  private long calcArthimeticImm32(int funct3, int funct7, int op1, int op2) {
    var shamt = op2 & 0x1f;
    return switch (funct3) {
      case 0b000 -> op1 + op2;
      case 0b001 -> op1 << shamt;
      case 0b101 ->
          switch (funct7) {
            case 0b0000000 -> op1 >>> shamt;
            case 0b0100000 -> op1 >> shamt;
            default -> throw IllegalInstructionException.create("Invalid funct7 %d", funct7);
          };
      case 0b110 -> op1 | op2;
      case 0b111 -> op1 & op2;
      default -> throw IllegalInstructionException.create("Invalid funct3 %d", funct3);
    };
  }

  private boolean calcComparsion(int funct3, long op1, long op2) {
    return switch (funct3) {
      case 0b000 -> op1 == op2;
      case 0b001 -> op1 != op2;
      case 0b100 -> op1 < op2;
      case 0b101 -> op1 >= op2;
      case 0b110 -> Long.compareUnsigned(op1, op2) < 0;
      case 0b111 -> Long.compareUnsigned(op1, op2) >= 0;
      default -> throw IllegalInstructionException.create("Invalid funct3 %d", funct3);
    };
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
