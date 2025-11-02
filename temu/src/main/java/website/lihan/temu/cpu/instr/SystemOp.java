package website.lihan.temu.cpu.instr;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.temu.Rv64Context;
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.HaltException;
import website.lihan.temu.cpu.IllegalInstructionException;
import website.lihan.temu.cpu.InterruptException;
import website.lihan.temu.cpu.JumpException;
import website.lihan.temu.cpu.PrivilegeLevel;
import website.lihan.temu.cpu.Opcodes.SystemFunct12;
import website.lihan.temu.cpu.Opcodes.SystemFunct3;
import website.lihan.temu.cpu.Opcodes.SystemFunct7;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.InterruptException.Cause;
import website.lihan.temu.cpu.RvUtils.IInstruct;
import website.lihan.temu.cpu.csr.CsrId;
import website.lihan.temu.cpu.csr.MStatus;
import website.lihan.temu.cpu.csr.SStatus;
import website.lihan.temu.sbi.Sbi;

public class SystemOp {
  public static void execute(Rv64Context context, Rv64State cpu, long pc, int instr, Node[] nodes) {
    if ((instr & 0x80) != 0) {
      doCsrRW(context, cpu, instr, nodes);
      return;
    }
    final var i = IInstruct.decode(instr);
    switch (i.funct3()) {
      case SystemFunct3.PRIV -> doPriv(context, cpu, pc, i);
      default -> throw IllegalInstructionException.create(pc, instr);
    }
  }

  private static void doCsrRW(Rv64Context context, Rv64State cpu, int instr, Node[] nodes) {
    final var csrNode = CsrRWNode.class.cast(nodes[instr >> 8]);
    switch (csrNode.funct3) {
      case SystemFunct3.CSRRW -> {
        var oldRegValue = cpu.getReg(csrNode.rs1);
        var oldCSRValue = csrNode.read();
        cpu.setReg(csrNode.rd, oldCSRValue);
        csrNode.write(oldRegValue);
        if (csrNode.csrId == CsrId.SATP) {
          MemoryAccess.printMapping(context, oldRegValue);
        }
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
        MemoryAccess.printMapping(context, cpu.getCsrFile().satp.getValue());
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
          case M -> throw InterruptException.create(pc,
        InterruptException.Cause.ECALL_FROM_M_MODE);
          default -> throw CompilerDirectives.shouldNotReachHere();
        }
      }
      case SystemFunct12.EBREAK -> throw HaltException.create(pc, cpu.getReg(10));
      case SystemFunct12.SRET -> {
        final var mstatus = new MStatus(cpu.getCsrFile().mstatus.getValue());
        final var sstatus = new SStatus(mstatus);
        final var sepc = cpu.getCsrFile().sepc.getValue();
        final var priv = sstatus.getSPP() == 1 ? PrivilegeLevel.S : PrivilegeLevel.U;
        sstatus.setSIE(sstatus.getSPIE());
        sstatus.setSPIE(true);
        sstatus.setSPP(0);
        cpu.setPrivilegeLevel(priv);
        Utils.printf("SRET to pc=%08x, priv=%s\n", sepc, priv);
        cpu.getCsrFile().sstatus.setValue(sstatus.getValue());
        throw JumpException.create(sepc);
      }
      case SystemFunct12.MRET -> {
        final var mstatus = new MStatus(cpu.getCsrFile().mstatus.getValue());
        final var mepc = cpu.getCsrFile().mepc;
        mstatus.setMIE(mstatus.getMPIE());
        mstatus.setMPIE(true);
        cpu.getCsrFile().mstatus.setValue(mstatus.getValue());
        throw JumpException.create(mepc.getValue());
      }
      default ->
          throw IllegalInstructionException.create(
              "Unsupported PRIV funct12=%03x at pc=%08x", i.imm(), pc);
    }
  }

  public static long doInterrupt(Rv64State cpu, InterruptException e) {
    var mode = handlingMode(cpu, e.cause);
    switch (mode) {
      case M -> {
        cpu.getCsrFile().mepc.setValue(e.pc);
        cpu.getCsrFile().mcause.setValue(e.cause);
        final var mstatus = new MStatus(cpu.getCsrFile().mstatus.getValue());
        mstatus.setMPIE(mstatus.getMIE());
        mstatus.setMIE(false);
        cpu.getCsrFile().mstatus.setValue(mstatus.getValue());
        cpu.setPrivilegeLevel(mode);
        return cpu.getCsrFile().mtvec.getValue();
      }
      case S -> {
        cpu.getCsrFile().sepc.setValue(e.pc);
        cpu.getCsrFile().scause.setValue(e.cause);
        cpu.getCsrFile().stval.setValue(e.stval);
        final var mstatus = new MStatus(cpu.getCsrFile().mstatus.getValue());
        final var sstatus = new SStatus(mstatus);
        sstatus.setSPP(cpu.getPrivilegeLevel() == PrivilegeLevel.S ? 1 : 0);
        sstatus.setSPIE(sstatus.getSIE());
        sstatus.setSIE(false);
        cpu.getCsrFile().sstatus.setValue(sstatus.getValue());
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
        // var medeleg = cpu.getCsrFile().medeleg.getValue();
        // Assumes all interrupts are delegated to S-mode
        var medeleg = 0xFFFFL;
        if ((medeleg & (1L << cause)) != 0) {
          yield PrivilegeLevel.S;
        } else {
          yield PrivilegeLevel.M;
        }
      }
    };
  }
}
