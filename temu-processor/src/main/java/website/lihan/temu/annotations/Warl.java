package website.lihan.temu.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a bit field as WARL (Write Any, Read Legal). Writes are checked against a set of legal
 * values.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Warl {
  /** An array of legal values that can be written to this field. */
  long[] values();
}
