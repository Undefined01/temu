package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.HostCompilerDirectives.BytecodeInterpreterSwitch;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.BytecodeOSRNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;

import website.lihan.temu.Bus;

public class Riscv64BytecodeNode extends Node implements BytecodeOSRNode {
  @CompilationFinal long pc;
  public long register[] = new long[32];
  @Child Bus bus;
  @CompilationFinal private Object osrMetadata;

  public Riscv64BytecodeNode(Bus bus) {
    this.bus = bus;
    this.pc = 0x80000000L;
  }

  public Object execute(VirtualFrame frame) {
    return executeFromPc(frame, this.pc);
  }

  @Override
  public Object executeOSR(VirtualFrame osrFrame, long target, Object interpreterState) {
    return executeFromPc(osrFrame, target);
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
  Object executeFromPc(VirtualFrame frame, long pc) {
    byte[] bytecode = new byte[4];
    int ii=1;
    while (true) {
      if (ii++ > 1000) {
        return 0;
      }
      CompilerAsserts.partialEvaluationConstant(pc);
      bus.executeRead(pc, bytecode, 4);
      int instr = bytecode[0] & 0xff | (bytecode[1] & 0xff) << 8 | (bytecode[2] & 0xff) << 16
          | (bytecode[3] & 0xff) << 24;
      CompilerAsserts.partialEvaluationConstant(instr);
      long nextPc = pc + 4;
      int opcode = instr & 0x7f;
      switch (opcode) {
        case Opcodes.ARITHMETIC_IMM -> {
          var i = IInstruct.decode(instr);
          var res = calcArthimetic(i.funct3, 0, register[i.rs1], i.imm);
          setRegister(i.rd, res);
        }
        case Opcodes.ARITHMETIC -> {
          var r = RInstruct.decode(instr);
          var res = calcArthimetic(r.func3, r.func7, register[r.rs1], register[r.rs2]);
          setRegister(r.rd, res);
        }
        case Opcodes.ARITHMETIC32 -> {
          var i = IInstruct.decode(instr);
          var res = calcArthimetic(i.funct3, 0, register[i.rs1], i.imm);
          res = signExtend(res, 32);
          setRegister(i.rd, res);
        }
        case Opcodes.ARITHMETIC32_IMM -> {
          var r = RInstruct.decode(instr);
          var res = calcArthimetic(r.func3, r.func7, register[r.rs1], register[r.rs2]);
          res = signExtend(res, 32);
          setRegister(r.rd, res);
        }
        case Opcodes.LUI -> {
          var u = UInstruct.decode(instr);
          setRegister(u.rd, u.imm);
        }
        case Opcodes.AUIPC -> {
          var u = UInstruct.decode(instr);
          setRegister(u.rd, pc + u.imm);
        }
        case Opcodes.JAL -> {
          var j = JInstruct.decode(instr);
          nextPc = pc + j.imm;
          setRegister(j.rd, pc + 4);
        }
        case Opcodes.JALR -> {
          var i = IInstruct.decode(instr);
          nextPc = register[i.rs1] + i.imm;
          setRegister(i.rd, pc + 4);
        }
        case Opcodes.BRANCH -> {
          nextPc = doBranch(pc, instr);
        }
        case Opcodes.LOAD -> doLoad(pc, instr);
        case Opcodes.STORE -> doStore(instr);
        case Opcodes.HALT -> {
          return 0;
        }
        default -> throw IllegalInstructionException.create(pc, instr);
      }

      CompilerAsserts.partialEvaluationConstant(nextPc);
      if (CompilerDirectives.inInterpreter() && nextPc < pc) { // back-edge
        if (BytecodeOSRNode.pollOSRBackEdge(this)) { // OSR can be tried
          Object result = BytecodeOSRNode.tryOSR(this, nextPc, null, null, frame);
          System.err.printf("Result %s\n", result);
          if (result != null) { // OSR was performed
            return result;
          }
        }
      }
      pc = nextPc;
    }
  }

  private long calcArthimetic(int func3, int func7, long op1, long op2) {
    return switch (func3) {
      case 0b000 -> {
        if (func7 == 0) {
          yield op1 + op2;
        } else {
          yield op1 - op2;
        }
      }
      case 0b001 -> op1 << op2;
      case 0b010 -> op1 < op2 ? 1 : 0;
      case 0b011 -> Long.compareUnsigned(op1, op2) < 0 ? 1 : 0;
      case 0b100 -> op1 ^ op2;
      case 0b101 -> {
        op2 &= 0x3f;
        if (func7 == 0) {
          yield op1 >>> op2;
        } else {
          yield op1 >> op2;
        }
      }
      case 0b110 -> op1 | op2;
      case 0b111 -> op1 & op2;
      default -> throw InternalException.create(String.valueOf(func3));
    };
  }

  private void setRegister(int rd, long value) {
    if (rd != 0) {
      register[rd] = value;
    }
  }

  private boolean calcComparsion(int func3, long op1, long op2) {
    return switch (func3) {
      case 0b000 -> op1 == op2;
      case 0b001 -> op1 != op2;
      case 0b100 -> op1 < op2;
      case 0b101 -> op1 >= op2;
      case 0b110 -> op1 < op2;
      case 0b111 -> op1 >= op2;
      default -> throw InternalException.create(String.valueOf(func3));
    };
  }

  private long doBranch(long pc, int instr) {
    var b = BInstruct.decode(instr);
    var cond = calcComparsion(b.funct3, register[b.rs1], register[b.rs2]);
    if (cond) {
      return pc + b.imm;
    } else {
      return pc + 4;
    }
  }

  private void doLoad(long pc, int instr) {
    var i = IInstruct.decode(instr);
    byte[] data = new byte[8];
    int length = switch (i.funct3 & 0b11) {
      case 0b00 -> 1;
      case 0b01 -> 2;
      case 0b10 -> 4;
      case 0b11 -> 8;
      default -> throw IllegalInstructionException.create(pc, instr);
    };
    bus.executeRead(register[i.rs1] + i.imm, data, length);
    long value = 0;
    for (int j = 0; j < length; j++) {
      value |= (long)(data[j] & 0xff) << (j * 8);
    }
    if ((i.funct3 & 0b100) == 0) {
      value = signExtend(value, length * 8);
    }
    setRegister(i.rd, value);
  }

  @ExplodeLoop
  private void doStore(int instr) {
    var s = SInstruct.decode(instr);
    byte[] data = new byte[8];
    int length = switch (s.funct3 & 0b11) {
      case 0b00 -> 1;
      case 0b01 -> 2;
      case 0b10 -> 4;
      case 0b11 -> 8;
      default -> throw IllegalInstructionException.create(pc, instr);
    };
    long value = register[s.rs2];
    for (int i = 0; i < length; i++) {
      data[i] = (byte) (value >> (i * 8));
    }
    bus.executeWrite(register[s.rs1] + s.imm, data, length);
  }

  private static long signExtend(long value, int bits) {
    return (value << (64 - bits)) >> (64 - bits);
  }

  static record IInstruct(int opcode, int rd, int rs1, int funct3, int imm) {
    public static IInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var rd = (instr >> 7) & 0x1f;
      var funct3 = (instr >> 12) & 0x7;
      var rs1 = (instr >> 15) & 0x1f;
      var imm = (instr >> 20);
      return new IInstruct(opcode, rd, rs1, funct3, imm);
    }
  }

  static record RInstruct(int opcode, int rd, int rs1, int rs2, int func3, int func7) {
    public static RInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var rd = (instr >> 7) & 0x1f;
      var func3 = (instr >> 12) & 0x7;
      var rs1 = (instr >> 15) & 0x1f;
      var rs2 = (instr >> 20) & 0x1f;
      var func7 = (instr >> 25);
      return new RInstruct(opcode, rd, rs1, rs2, func3, func7);
    }
  }

  static record UInstruct(int opcode, int rd, int imm) {
    public static UInstruct decode(int instr) {
      var opcode = instr & 0x7f;
      var rd = (instr >> 7) & 0x1f;
      var imm = instr & 0xfffff000;
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
      var imm = imm11_5 | imm4_0;
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
      var imm = imm12 | imm11 | imm10_5 | imm4_1;
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
      var imm = imm20 | imm19_12 | imm11 | imm10_1;
      return new JInstruct(opcode, rd, imm);
    }
  }
}
