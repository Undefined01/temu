package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.HostCompilerDirectives.BytecodeInterpreterSwitch;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.BytecodeOSRNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;

import website.lihan.temu.bus.Bus;

public class Rv64BytecodeNode extends Node implements BytecodeOSRNode {
  @CompilationFinal long baseAddr;
  @CompilationFinal int entryBci;
  public long register[] = new long[32];
  @Child Bus bus;
  @CompilationFinal(dimensions=1) private byte[] bc;
  @CompilationFinal private Object osrMetadata;

  public Rv64BytecodeNode(Bus bus) {
    this.bus = bus;
    this.bc = new byte[4 * 1024];
    this.baseAddr = 0x80000000L;
    bus.executeRead(baseAddr, bc, bc.length);
    this.entryBci = 0;
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
  Object executeFromBci(VirtualFrame frame, int bci) {
    byte[] bytecode = new byte[4];
    while (true) {
      CompilerAsserts.partialEvaluationConstant(bci);
      // bus.executeRead(pc, bytecode, 4);
      System.arraycopy(bc, bci, bytecode, 0, 4);
      int instr =
          bytecode[0] & 0xff
              | (bytecode[1] & 0xff) << 8
              | (bytecode[2] & 0xff) << 16
              | (bytecode[3] & 0xff) << 24;
      CompilerAsserts.partialEvaluationConstant(instr);

      int nextBci = executeOneInstr(frame, bci, instr);
      CompilerAsserts.partialEvaluationConstant(nextBci);
      if (CompilerDirectives.inInterpreter() && nextBci < bci) { // back-edge
        if (BytecodeOSRNode.pollOSRBackEdge(this)) { // OSR can be tried
          Object result = BytecodeOSRNode.tryOSR(this, nextBci, null, null, frame);
          System.err.printf("Result %s\n", result);
          if (result != null) { // OSR was performed
            return result;
          }
        }
      }
      bci = nextBci;
    }
  }

  private int executeOneInstr(VirtualFrame frame, int bci, int instr) {
    int opcode = instr & 0x7f;
    switch (opcode) {
      case Opcodes.ARITHMETIC_IMM -> {
        var i = IInstruct.decode(instr);
        var res = calcArthimetic(i.funct3, 0, getRegister(i.rs1), i.imm);
        setRegister(i.rd, res);
      }
      case Opcodes.ARITHMETIC -> {
        var r = RInstruct.decode(instr);
        var res = calcArthimetic(r.func3, r.func7, getRegister(r.rs1), getRegister(r.rs2));
        setRegister(r.rd, res);
      }
      case Opcodes.ARITHMETIC32 -> {
        var i = IInstruct.decode(instr);
        var res = calcArthimetic(i.funct3, 0, getRegister(i.rs1), i.imm);
        res = signExtend(res, 32);
        setRegister(i.rd, res);
      }
      case Opcodes.ARITHMETIC32_IMM -> {
        var r = RInstruct.decode(instr);
        var res = calcArthimetic(r.func3, r.func7, getRegister(r.rs1), getRegister(r.rs2));
        res = signExtend(res, 32);
        setRegister(r.rd, res);
      }
      case Opcodes.LUI -> {
        var u = UInstruct.decode(instr);
        setRegister(u.rd, u.imm);
      }
      case Opcodes.AUIPC -> {
        var u = UInstruct.decode(instr);
        setRegister(u.rd, bci + u.imm);
      }
      case Opcodes.JAL -> {
        var j = JInstruct.decode(instr);
        int nextBci = bci + j.imm;
        setRegister(j.rd, bci + 4);
        return tryJump(nextBci);
      }
      case Opcodes.JALR -> {
        var i = IInstruct.decode(instr);
        var nextPc = getRegister(i.rs1) + i.imm;
        setRegister(i.rd, bci + 4);
        return tryJumpPc(nextPc);
      }
      case Opcodes.BRANCH -> {
        var b = BInstruct.decode(instr);
        var cond = calcComparsion(b.funct3, getRegister(b.rs1), getRegister(b.rs2));
        if (cond) {
          int nextBci = bci + b.imm;
          return tryJump(nextBci);
        }
      }
      case Opcodes.LOAD -> doLoad(bci, instr);
      case Opcodes.STORE -> doStore(instr);
      case Opcodes.HALT -> {
        return 0;
      }
      default -> throw IllegalInstructionException.create(bci, instr);
    }
    return tryJump(bci + 4);
  }

  private int tryJump(int nextBci) {
    if (nextBci >= 0 || nextBci < bc.length) {
      return nextBci;
    }
    throw JumpException.create(getPc(nextBci));
  }

  private int tryJumpPc(long nextPc) {
    if (nextPc >= baseAddr || nextPc < baseAddr + bc.length) {
      return (int) (nextPc - baseAddr);
    }
    throw JumpException.create(nextPc);
  }

  private long getPc(int bci) {
    return baseAddr + (long)bci;
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

  public long getRegister(int rs) {
    if (rs != 0) {
      return register[rs];
    } else {
      return 0;
    }
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
    bus.executeRead(getRegister(i.rs1) + i.imm, data, length);
    long value = 0;
    for (int j = 0; j < length; j++) {
      value |= (long) (data[j] & 0xff) << (j * 8);
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
    int length =
        switch (s.funct3 & 0b11) {
          case 0b00 -> 1;
          case 0b01 -> 2;
          case 0b10 -> 4;
          case 0b11 -> 8;
          default -> throw IllegalInstructionException.create(entryBci, instr);
        };
    long value = getRegister(s.rs2);
    for (int i = 0; i < length; i++) {
      data[i] = (byte) (value >> (i * 8));
    }
    bus.executeWrite(getRegister(s.rs1) + s.imm, data, length);
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
