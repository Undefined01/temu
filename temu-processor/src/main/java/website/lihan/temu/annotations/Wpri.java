package website.lihan.temu.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a bit field as WPRI (Write Preserve, Read Ignored). Writes to this field are ignored. */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Wpri {}
