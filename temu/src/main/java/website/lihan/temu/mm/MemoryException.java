package website.lihan.temu.mm;

import com.oracle.truffle.api.CompilerDirectives;
import website.lihan.temu.cpu.InterruptException.Cause;

public enum MemoryException {
  AccessFault,
  PageFault,
  ;

  public long toLoadException() {
    return switch (this) {
      case AccessFault -> Cause.LOAD_ACCESS_FAULT;
      case PageFault -> Cause.LOAD_PAGE_FAULT;
      default -> throw CompilerDirectives.shouldNotReachHere();
    };
  }

  public long toStoreException() {
    return switch (this) {
      case AccessFault -> Cause.STORE_ACCESS_FAULT;
      case PageFault -> Cause.STORE_PAGE_FAULT;
      default -> throw CompilerDirectives.shouldNotReachHere();
    };
  }

  public long toExecuteException() {
    return switch (this) {
      case AccessFault -> Cause.INST_ACCESS_FAULT;
      case PageFault -> Cause.INSTRUCTION_PAGE_FAULT;
      default -> throw CompilerDirectives.shouldNotReachHere();
    };
  }
}
