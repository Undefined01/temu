package website.lihan.temu.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotates a field within a @RiscvCsr class that represents a bit field. */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface BitField {
  /** The starting bit position (offset) of the field. */
  int offset();

  /** The number of bits in the field. */
  int length();

  long resetValue() default 0;
}
