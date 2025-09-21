package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.HostCompilerDirectives.BytecodeInterpreterSwitch;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.memory.ByteArraySupport;
import com.oracle.truffle.api.nodes.BytecodeOSRNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.Utils;
import website.lihan.temu.device.Bus;

public class Rv64BytecodeNode extends Node implements BytecodeOSRNode {
  @CompilationFinal long baseAddr;
  @CompilationFinal int entryBci;
  @CompilationFinal Rv64State cpu;
  @Child Bus bus;

  @CompilationFinal(dimensions = 1)
  private byte[] bc;

  @CompilationFinal private Object osrMetadata;

  private static final ByteArraySupport BYTES = ByteArraySupport.littleEndian();

  public Rv64BytecodeNode(long baseAddr, byte[] bc, int entryBci) {
    this.baseAddr = baseAddr;
    this.bc = bc;
    this.entryBci = entryBci;

    var context = Rv64Context.get(this);
    this.cpu = context.getState();
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
  public Object executeFromBci(VirtualFrame frame, int bci_) {
    int bci = bci_;
    while (true) {
      CompilerAsserts.partialEvaluationConstant(bci);
      int instr = BYTES.getInt(bc, bci);
      // CompilerAsserts.partialEvaluationConstant(instr);

      int nextBci = bci + 4;

      int opcode = instr & 0x7f;
      switch (opcode) {
        case Opcodes.OP_IMM -> {
          var i = IInstruct.decode(instr);
          var res = calcArthimeticImm(i.funct3, i.funct7, cpu.getReg(i.rs1), i.imm);
          cpu.setReg(i.rd, res);
        }
        case Opcodes.OP -> {
          var r = RInstruct.decode(instr);
          var res = calcArthimetic(r.funct3, r.funct7, cpu.getReg(r.rs1), cpu.getReg(r.rs2));
          cpu.setReg(r.rd, res);
        }
        case Opcodes.OP_IMM_32 -> {
          var i = IInstruct.decode(instr);
          long res = calcArthimeticImm32(i.funct3, i.funct7, (int) cpu.getReg(i.rs1), i.imm);
          res = signExtend(res, 32);
          cpu.setReg(i.rd, res);
        }
        case Opcodes.OP_32 -> {
          var r = RInstruct.decode(instr);
          long res =
              calcArthimetic32(
                  r.funct3, r.funct7, (int) cpu.getReg(r.rs1), (int) cpu.getReg(r.rs2));
          res = signExtend(res, 32);
          cpu.setReg(r.rd, res);
        }
        case Opcodes.LUI -> {
          final var u = UInstruct.decode(instr);
          cpu.setReg(u.rd, u.imm);
        }
        case Opcodes.AUIPC -> {
          final var u = UInstruct.decode(instr);
          cpu.setReg(u.rd, getPc(bci) + u.imm);
        }
        case Opcodes.JAL -> {
          final var j = JInstruct.decode(instr);
          var imm20 = ((instr >> 31) & 0x1) << 20;
          var imm10_1 = ((instr >> 21) & 0x3ff) << 1;
          var imm11 = ((instr >> 20) & 0x1) << 11;
          var imm19_12 = ((instr >> 12) & 0xff) << 12;
          var imm = signExtend32(imm20 | imm19_12 | imm11 | imm10_1, 21);
          nextBci = bci + imm;
          cpu.setReg(j.rd, getPc(bci + 4));
        }
        case Opcodes.JALR -> {
          final var i = IInstruct.decode(instr);
          final var imm = (instr >> 20);
          final var nextPc = cpu.getReg(i.rs1) + (long) imm;
          cpu.setReg(i.rd, getPc(bci + 4));
          nextBci = (int) (nextPc - baseAddr);
          throw JumpException.create(nextBci + baseAddr);
        }
        case Opcodes.BRANCH -> {
          final var b = BInstruct.decode(instr);
          var cond = calcComparsion(b.funct3, cpu.getReg(b.rs1), cpu.getReg(b.rs2));
          if (cond) {
            var imm12 = (instr >> 31) << 12;
            var imm11 = ((instr >> 7) & 0x1) << 11;
            var imm10_5 = ((instr >> 25) & 0x3f) << 5;
            var imm4_1 = ((instr >> 8) & 0xf) << 1;
            var imm = signExtend32(imm12 | imm11 | imm10_5 | imm4_1, 13);
            nextBci = bci + imm;
          }
        }
        case Opcodes.LOAD -> doLoad(bci, instr);
        case Opcodes.STORE -> doStore(instr);
        case Opcodes.EBREAK -> {
          throw HaltException.create(getPc(bci), cpu.getReg(10));
        }
        default -> throw IllegalInstructionException.create(bci, instr);
      }

      // if (CompilerDirectives.inInterpreter() && nextBci < bci) { // back-edge
      //   if (BytecodeOSRNode.pollOSRBackEdge(this, 1)) { // OSR can be tried
      //     Object result = BytecodeOSRNode.tryOSR(this, nextBci, null, null, frame);
      //     if (result != null) { // OSR was performed
      //       return result;
      //     }
      //   }
      // }
      bci = nextBci;
    }
  }

  private int executeOneInstr(VirtualFrame frame, int bci, int instr) {
    return bci + 4;
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

  private void doLoad(long pc, int instr) {
    var i = IInstruct.decode(instr);
    byte[] data = new byte[8];
    int length =
        switch (i.funct3 & 0b11) {
          case 0b00 -> 1;
          case 0b01 -> 2;
          case 0b10 -> 4;
          case 0b11 -> 8;
          default -> throw IllegalInstructionException.create(pc, instr);
        };
    bus.executeRead(cpu.getReg(i.rs1) + i.imm, data, length);
    long value = 0;
    for (int j = 0; j < length; j++) {
      value |= (long) (data[j] & 0xff) << (j * 8);
    }
    if ((i.funct3 & 0b100) == 0) {
      value = signExtend(value, length * 8);
    }
    cpu.setReg(i.rd, value);
  }

  @ExplodeLoop
  private void doStore(int instr) {
    var s = SInstruct.decode(instr);
    byte[] data = new byte[8];
    int length =
        switch (s.funct3 & 0b11) {
          case 0b00 -> 1;
          case 0b01 -> 2;
          case 0b10 -> 4;
          case 0b11 -> 8;
          default -> throw IllegalInstructionException.create(entryBci, instr);
        };
    long value = cpu.getReg(s.rs2);
    BYTES.putLong(data, 0, value);
    bus.executeWrite(cpu.getReg(s.rs1) + s.imm, data, length);
  }

  private static long signExtend(long value, int bits) {
    return (value << (64 - bits)) >> (64 - bits);
  }

  private static int signExtend32(int value, int bits) {
    return (value << (64 - bits)) >> (64 - bits);
  }

  static record IInstruct(int opcode, int rd, int rs1, int funct3, int funct7, int imm) {
    public static IInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var rd = (instr >> 7) & 0x1f;
      var funct3 = (instr >> 12) & 0x7;
      var funct7 = (instr >> 25) & 0x7f;
      var rs1 = (instr >> 15) & 0x1f;
      var imm = signExtend32(instr >> 20, 12);
      return new IInstruct(opcode, rd, rs1, funct3, funct7, imm);
    }
  }

  static record RInstruct(int opcode, int rd, int rs1, int rs2, int funct3, int funct7) {
    public static RInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var rd = (instr >> 7) & 0x1f;
      var funct3 = (instr >> 12) & 0x7;
      var rs1 = (instr >> 15) & 0x1f;
      var rs2 = (instr >> 20) & 0x1f;
      var funct7 = (instr >> 25);
      return new RInstruct(opcode, rd, rs1, rs2, funct3, funct7);
    }
  }

  static record UInstruct(int opcode, int rd, int imm) {
    public static UInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var rd = (instr >> 7) & 0x1f;
      var imm = (instr >> 12) << 12;
      return new UInstruct(opcode, rd, imm);
    }
  }

  static record SInstruct(int opcode, int rs1, int rs2, int funct3, int imm) {
    public static SInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var imm11_5 = ((instr >> 25) & 0x7f) << 5;
      var imm4_0 = (instr >> 7) & 0x1f;
      var funct3 = (instr >> 12) & 0x7;
      var rs1 = (instr >> 15) & 0x1f;
      var rs2 = (instr >> 20) & 0x1f;
      var imm = signExtend32(imm11_5 | imm4_0, 12);
      return new SInstruct(opcode, rs1, rs2, funct3, imm);
    }
  }

  static record BInstruct(int opcode, int rs1, int rs2, int funct3, int imm) {
    public static BInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var funct3 = (instr >> 12) & 0x7;
      var rs1 = (instr >> 15) & 0x1f;
      var rs2 = (instr >> 20) & 0x1f;
      var imm12 = (instr >> 31) << 12;
      var imm11 = ((instr >> 7) & 0x1) << 11;
      var imm10_5 = ((instr >> 25) & 0x3f) << 5;
      var imm4_1 = ((instr >> 8) & 0xf) << 1;
      var imm = signExtend32(imm12 | imm11 | imm10_5 | imm4_1, 13);
      return new BInstruct(opcode, rs1, rs2, funct3, imm);
    }
  }

  static record JInstruct(int opcode, int rd, int imm) {
    public static JInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var rd = (instr >> 7) & 0x1f;
      var imm20 = (instr >> 31) << 20;
      var imm10_1 = ((instr >> 21) & 0x3ff) << 1;
      var imm11 = ((instr >> 20) & 0x1) << 11;
      var imm19_12 = ((instr >> 12) & 0xff) << 12;
      var imm = signExtend32(imm20 | imm19_12 | imm11 | imm10_1, 21);
      return new JInstruct(opcode, rd, imm);
    }
  }
}
