package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.cpu.HaltException;
import website.lihan.temu.cpu.IllegalInstructionException;
import website.lihan.temu.cpu.InterruptException;
import website.lihan.temu.cpu.JumpException;
import website.lihan.temu.cpu.Opcodes.SystemFunct12;
import website.lihan.temu.cpu.Opcodes.SystemFunct3;
import website.lihan.temu.cpu.Opcodes.SystemFunct7;
import website.lihan.temu.cpu.PrivilegeLevel;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.RvUtils.IInstruct;
import website.lihan.temu.cpu.sbi.Sbi;

public final class SystemOp {
  public static void execute(Rv64Context context, Rv64State cpu, long pc, int instr, Node[] nodes) {
    if ((instr & 0x80) != 0) {
      doCsrRW(context, cpu, instr, nodes);
      return;
    }
    final var i = IInstruct.decode(instr);
    switch (i.funct3()) {
      case SystemFunct3.PRIV -> doPriv(context, cpu, pc, i);
      default ->
          throw IllegalInstructionException.create(
              pc, "Unsupported SYSTEM funct3=%03x", i.funct3());
    }
  }

  private static void doCsrRW(Rv64Context context, Rv64State cpu, int instr, Node[] nodes) {
    final var csrNode = CsrRWNode.class.cast(nodes[instr >> 8]);
    csrNode.checkPrivilege(cpu);
    switch (csrNode.funct3) {
      case SystemFunct3.CSRRW -> {
        var oldRegValue = cpu.getReg(csrNode.rs1);
        var oldCSRValue = csrNode.read();
        cpu.setReg(csrNode.rd, oldCSRValue);
        csrNode.write(oldRegValue);
      }
      case SystemFunct3.CSRRS -> {
        var oldRegValue = cpu.getReg(csrNode.rs1);
        var oldCSRValue = csrNode.read();
        cpu.setReg(csrNode.rd, oldCSRValue);
        if (csrNode.rs1 != 0) {
          csrNode.write(oldCSRValue | oldRegValue);
        }
      }
      case SystemFunct3.CSRRC -> {
        var oldRegValue = cpu.getReg(csrNode.rs1);
        var oldCSRValue = csrNode.read();
        cpu.setReg(csrNode.rd, oldCSRValue);
        if (csrNode.rs1 != 0) {
          csrNode.write(oldCSRValue & ~oldRegValue);
        }
      }
      case SystemFunct3.CSRRWI -> {
        var oldCSRValue = csrNode.read();
        cpu.setReg(csrNode.rd, oldCSRValue);
        csrNode.write(csrNode.rs1);
      }
      case SystemFunct3.CSRRSI -> {
        var oldCSRValue = csrNode.read();
        cpu.setReg(csrNode.rd, oldCSRValue);
        if (csrNode.rs1 != 0) {
          csrNode.write(oldCSRValue | csrNode.rs1);
        }
      }
      case SystemFunct3.CSRRCI -> {
        var oldCSRValue = csrNode.read();
        cpu.setReg(csrNode.rd, oldCSRValue);
        if (csrNode.rs1 != 0) {
          csrNode.write(oldCSRValue & ~csrNode.rs1);
        }
      }
      default -> throw CompilerDirectives.shouldNotReachHere();
    }
  }

  private static void doPriv(Rv64Context context, Rv64State cpu, long pc, IInstruct i) {
    switch (i.funct7()) {
      case SystemFunct7.SFENCE_VMA -> {
        context.execPageCache.clear();
        throw JumpException.create(pc + 4);
      }
    }
    switch (i.imm()) {
      case SystemFunct12.ECALL -> {
        switch (cpu.getPrivilegeLevel()) {
          case U -> throw InterruptException.create(pc, InterruptException.Cause.ECALL_FROM_U_MODE);
          case S -> {
            Sbi.handle(cpu);
            // throw InterruptException.create(pc, InterruptException.Cause.ECALL_FROM_S_MODE);
          }
          case M -> throw InterruptException.create(pc, InterruptException.Cause.ECALL_FROM_M_MODE);
          default -> throw CompilerDirectives.shouldNotReachHere();
        }
      }
      case SystemFunct12.EBREAK -> throw HaltException.create(pc, cpu.getReg(10));
      case SystemFunct12.SRET -> {
        if (cpu.getPrivilegeLevel() == PrivilegeLevel.U) {
          throw IllegalInstructionException.create(pc, "SRET called from non-S privilege level");
        }
        final var sstatus = cpu.getCsrFile().sstatus;
        final var sepc = cpu.getCsrFile().sepc;
        final var priv = sstatus.getSPP() == 1 ? PrivilegeLevel.S : PrivilegeLevel.U;
        sstatus.setSIE(sstatus.getSPIE());
        sstatus.setSPIE(true);
        sstatus.setSPP(0);
        cpu.setPrivilegeLevel(priv);
        throw JumpException.create(sepc.getValue());
      }
      case SystemFunct12.MRET -> {
        if (cpu.getPrivilegeLevel() != PrivilegeLevel.M) {
          throw IllegalInstructionException.create(pc, "MRET called from non-M privilege level");
        }
        final var mstatus = cpu.getCsrFile().mstatus;
        final var mepc = cpu.getCsrFile().mepc;
        final var priv = mstatus.getMPP();
        if (priv != 0b11) {
          // mstatus.setMPRV(0);
        }
        mstatus.setMIE(mstatus.getMPIE());
        mstatus.setMPIE(true);
        mstatus.setMPP(0);
        cpu.setPrivilegeLevel(PrivilegeLevel.from(priv));
        throw JumpException.create(mepc.getValue());
      }
      default ->
          throw IllegalInstructionException.create(pc, "Unsupported PRIV funct12=%03x", i.imm());
    }
  }

  public static long doInterrupt(Rv64State cpu, InterruptException e) {
    var mode = handlingMode(cpu, e.cause);
    switch (mode) {
      case M -> {
        cpu.getCsrFile().mepc.setValue(e.pc);
        cpu.getCsrFile().mcause.setValue(e.cause);
        final var mstatus = cpu.getCsrFile().mstatus;
        mstatus.setMPP(cpu.getPrivilegeLevel().level());
        mstatus.setMPIE(mstatus.getMIE());
        mstatus.setMIE(false);
        cpu.setPrivilegeLevel(mode);
        return cpu.getCsrFile().mtvec.getValue();
      }
      case S -> {
        cpu.getCsrFile().sepc.setValue(e.pc);
        cpu.getCsrFile().scause.setValue(e.cause);
        cpu.getCsrFile().stval.setValue(e.stval);
        final var sstatus = cpu.getCsrFile().sstatus;
        sstatus.setSPP(cpu.getPrivilegeLevel() == PrivilegeLevel.S ? 1 : 0);
        sstatus.setSPIE(sstatus.getSIE());
        sstatus.setSIE(false);
        cpu.setPrivilegeLevel(mode);
        return cpu.getCsrFile().stvec.getValue();
      }
      default -> throw CompilerDirectives.shouldNotReachHere();
    }
  }

  public static PrivilegeLevel handlingMode(Rv64State cpu, long cause) {
    return switch (cpu.getPrivilegeLevel()) {
      case M -> PrivilegeLevel.M;
      case S, U -> {
        var medeleg = cpu.getCsrFile().medeleg.getValue();
        if ((medeleg & (1L << cause)) != 0) {
          yield PrivilegeLevel.S;
        } else {
          yield PrivilegeLevel.M;
        }
      }
      default -> throw CompilerDirectives.shouldNotReachHere();
    };
  }
}
