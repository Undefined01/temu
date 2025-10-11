package website.lihan.temu.cpu.instr;

import website.lihan.temu.cpu.HaltException;
import website.lihan.temu.cpu.IllegalInstructionException;
import website.lihan.temu.cpu.InterruptException;
import website.lihan.temu.cpu.JumpException;
import website.lihan.temu.cpu.Rv64State;
import website.lihan.temu.cpu.RvUtils;
import website.lihan.temu.cpu.RvUtils.IInstruct;

public class SystemOp {
  public static void execute(Rv64State cpu, int instr) {
    final var r = RvUtils.RInstruct.decode(instr);
    final var op1 = cpu.getReg(r.rs1());
    final var op2 = cpu.getReg(r.rs2());
    final var funct3 = r.funct3();
    final var funct7 = r.funct7();
    final var shamt = op2 & 0x3f;
    {
      final var i = IInstruct.decode(instr);
      switch (i.funct3()) {
        case 0b000 -> {
          switch (i.imm()) {
            case 0b000000000000 -> {
              // ECALL
              throw InterruptException.create(cpu.pc, 9);
            }
            case 0b000000000001 -> {
              // EBREAK
              throw HaltException.create(cpu.pc, cpu.getReg(10));
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
            default -> throw IllegalInstructionException.create(cpu.pc, instr);
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
        default -> throw IllegalInstructionException.create(cpu.pc, instr);
      }
    }
  }
}
