package website.lihan.temu.cpu.sbi;

import static website.lihan.temu.cpu.sbi.Sbi.ExtIDs.*;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import website.lihan.temu.Configuration;
import website.lihan.temu.Rv64Scope;
import website.lihan.temu.Utils;
import website.lihan.temu.cpu.PrivilegeLevel;
import website.lihan.temu.cpu.RegId;
import website.lihan.temu.cpu.Rv64State;

public final class Sbi {
  public static void enableEmulateSbi(Rv64Scope scope, Path dts) {
    Configuration.emulateSbi = true;
    try {
      var dtsContent = Files.readAllBytes(dts);
      var dtsAddr = 0x81000000L;
      scope.bus.executeWrite(dtsAddr, dtsContent, dtsContent.length);
      scope.context.state.setReg(RegId.a0, 0);
      scope.context.state.setReg(RegId.a1, dtsAddr);
      scope.context.state.setPrivilegeLevel(PrivilegeLevel.S);
      scope.context.state.getCsrFile().medeleg.setValue(0b1111_1111_1111_1111L);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @TruffleBoundary
  public static void handle(Rv64State cpu) {
    var ext = cpu.getReg(RegId.a7);
    var fid = cpu.getReg(RegId.a6);
    long error = 0;
    long value = 0;

    switch ((int) ext) {
      case CONSOLE_PUTCHAR -> {
        var ch = (byte) cpu.getReg(RegId.a0);
        System.out.print((char) ch);
      }
      default -> {
        Utils.printf("SBI call: ext=0x%08x, fid=0x%08x\n", ext, fid);
        error = -2; // SBI_ERR_NOT_SUPPORTED
      }
    }

    cpu.setReg(RegId.a0, error);
    cpu.setReg(RegId.a1, value);
  }

  public static class ExtIDs {
    // RISCV_SBI_V01
    public static final int SET_TIMER = 0x00;
    public static final int CONSOLE_PUTCHAR = 0x01;
    public static final int CONSOLE_GETCHAR = 0x02;
    public static final int CLEAR_IPI = 0x03;
    public static final int SEND_IPI = 0x04;
    public static final int REMOTE_FENCE_I = 0x05;
    public static final int REMOTE_SFENCE_VMA = 0x06;
    public static final int REMOTE_SFENCE_VMA_ASID = 0x07;
    public static final int SHUTDOWN = 0x08;

    public static final int BASE = 0x10;
    public static final int TIME = 0x54494D45;
    public static final int IPI = 0x735049;
    public static final int RFENCE = 0x52464E43;
    public static final int HSM = 0x48534D;
    public static final int SRST = 0x53525354;
    public static final int PMU = 0x504D55;
  }
}
