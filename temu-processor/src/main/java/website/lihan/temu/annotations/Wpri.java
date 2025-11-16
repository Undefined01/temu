package website.lihan.temu.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks a bit field as WPRI (Write Preserve, Read Ignored).
///
/// This mark are used to mark fields in CSRs that are preserved for future extensions.
///
/// From the software perspective, writes to this field should preserve the previous value,
/// and reads from this field should be ignored.
///
/// From the hardware perspective, this field should be hardwired to zero (or a constant value) for
// forward compatibility.
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Wpri {}
