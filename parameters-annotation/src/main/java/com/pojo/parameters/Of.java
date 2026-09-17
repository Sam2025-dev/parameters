package com.pojo.parameters;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * One or more assembled properties of the same Java class.
 *
 * <p>{@code class} is a Java keyword, so the type member is named {@code Class},
 * matching {@link Parameters#String()}. {@code names} is the property identifiers.
 *
 * <pre>{@code
 * @Of(Class = Address.class, names = {"homeAddress", "workAddress"})
 * @Of(Class = Long.class, names = {"id"})
 * @Of(Class = String[].class, names = {"tags"})
 * @Of(Class = Address.class)
 * }</pre>
 *
 * <p>When {@code names} is omitted, one field is generated from the decapitalized
 * simple class name ({@code Address} → {@code address}). Types whose simple name
 * is not a valid identifier (for example {@code Long} → {@code long}) must declare
 * {@code names} explicitly.
 */
@Documented
@Target({})
@Retention(RetentionPolicy.SOURCE)
public @interface Of {

    /**
     * Java class of each assembled property. Written as {@code Class = Address.class}.
     */
    Class<?> Class();

    /**
     * Property names of this class. Empty means one field named from the simple
     * class name.
     */
    String[] names() default {};
}
