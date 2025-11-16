package website.lihan.temu;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

public class Configuration {
  /// Intercept ecalls from S-mode and emulate SBI directly in the emulator.
  @CompilationFinal public static boolean emulateSbi = false;

  /// Print instruction trace information.
  @CompilationFinal public static boolean itrace = false;

  /// Enable itrace when executes to itraceStart
  @CompilationFinal public static long itraceStart = 0;

  /// Print function call/return trace information.
  @CompilationFinal public static boolean ftrace = true;

  /// Halt execution when an illegal instruction is encountered instead of trapping.
  /// This is useful for debugging emulator.
  @CompilationFinal public static boolean haltAtIllegalInstruction = true;
}
