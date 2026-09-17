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
 *     boolean_ = {true},
 *     type = Address.class,
 *     value = @Parameter(name = "id", type = Long.class)
 * )
 * @Data
 * public class UserVO extends UserVO__Parameters {
 *     private String extra;
 * }
 * }</pre>
 *
 * <p>Shorthands such as {@code int_ = {1, 2, 3}} generate initialized primitive
 * fields ({@code int_1 = 1}, …). {@code type = Address.class} names the field
 * from the decapitalized simple name ({@code address}). Use {@link Parameter}
 * when you need an explicit name or a type whose simple name is not a valid
 * identifier (for example {@code Long} → {@code long}).
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
     * Explicitly typed properties of any kind, including primitives, boxed
     * types, arrays, and custom classes.
     */
    Parameter[] value() default {};

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

    long[] long_() default {};

    char[] char_() default {};

    float[] float_() default {};

    double[] double_() default {};

    /**
     * Additional types to assemble. The field name is the decapitalized simple
     * name of each class ({@code Address} → {@code address}).
     */
    Class<?>[] type() default {};
}
