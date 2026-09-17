package com.pojo.parameters;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Compile-time property assembly for PO/VO types, in the same spirit as Lombok.
 *
 * <p>Place this on a class and extend the generated {@code <SimpleName>__Parameters}
 * superclass. The processor creates that superclass with:
 * <ul>
 *   <li>every instance field already declared on the annotated class (merged in)</li>
 *   <li>properties declared on this annotation: primitives, {@code String},
 *       arrays, boxed types, and custom classes</li>
 * </ul>
 *
 * <pre>{@code
 * @Parameters(
 *     String = {"userName"},
 *     int_ = {1, 2, 3},
 *     long_ = {18L},
 *     of = {
 *         @Of(Class = Address.class, names = {"homeAddress"}),
 *         @Of(Class = Long.class, names = {"id"})
 *     }
 * )
 * @Data
 * public class UserVO extends UserVO__Parameters {
 *     private String extra;
 * }
 * }</pre>
 *
 * <p>Shorthands such as {@code int_ = {1, 2, 3}} and {@code long_ = {18L}}
 * generate initialized primitive fields ({@code int_1 = 1}, {@code long_18 = 18L}).
 * {@code of} declares a Java class plus {@code String[]} names for that class.
 * When {@code names} is omitted, the field is the decapitalized simple class name
 * ({@code Address} → {@code address}).
 *
 * <p>Compatible with Lombok annotations such as {@code @Data}, {@code @Getter},
 * {@code @Setter}, {@code @Builder}, {@code @ToString}, and
 * {@code @EqualsAndHashCode}. Assembled properties live on the generated
 * superclass so the two processors do not emit duplicate members.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Parameters {

    /**
     * Properties of any Java class. Each {@link Of} names a class and the
     * {@code String[]} field names of that class.
     */
    Of[] of() default {};

    /**
     * Names of {@code String} properties. Written as
     * {@code @Parameters(String = {"userName"})}.
     */
    String[] String() default {};

    boolean[] boolean_() default {};

    byte[] byte_() default {};

    short[] short_() default {};

    /**
     * Initial values of assembled {@code int} properties. Each value {@code n}
     * becomes a field named {@code int_n} initialized to {@code n}.
     */
    int[] int_() default {};

    /**
     * Initial values of assembled {@code long} properties. Each value {@code n}
     * becomes a field named {@code long_n} initialized to {@code n}L.
     */
    long[] long_() default {};

    char[] char_() default {};

    float[] float_() default {};

    double[] double_() default {};
}
