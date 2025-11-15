package website.lihan.temu.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotates a class that represents a RISC-V Control and Status Register (CSR). */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface RiscvCsr {
  /** The address of the CSR. */
  int address();

  long resetValue() default 0;
}
