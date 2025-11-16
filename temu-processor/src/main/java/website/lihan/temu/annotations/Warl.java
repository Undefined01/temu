package website.lihan.temu.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks a bit field as WARL (Write Any, Read Legal).
///
/// Fields marked with WARL are only defined for a subset of bit encodings.
/// If a write is attempted with an illegal value, the write is ignored and the previous value is
/// preserved.
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Warl {
  /** An array of legal values that can be written to this field. */
  long[] values();
}
