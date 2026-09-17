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
 * superclass. The processor creates that superclass with the declared fields plus
 * JavaBean getters and setters.
 *
 * <pre>{@code
 * @Parameters(String = {"userName"}, int_ = {1, 2, 3})
 * @Data
 * public class UserVO extends UserVO__Parameters {
 * }
 * }</pre>
 *
 * <p>{@code String = {"userName"}} generates {@code private String userName}.
 * {@code int_ = {1, 2, 3}} generates {@code private int int_1 = 1},
 * {@code private int int_2 = 2}, and {@code private int int_3 = 3}.
 *
 * <p>Compatible with Lombok annotations such as {@code @Data}, {@code @Getter},
 * {@code @Setter}, {@code @Builder}, {@code @ToString}, and
 * {@code @EqualsAndHashCode}. Lombok continues to process fields declared on the
 * annotated class; assembled properties live on the generated superclass so the
 * two processors do not emit duplicate members.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Parameters {

    /**
     * Names of {@code String} properties to assemble onto the type.
     * {@code String} is a legal annotation attribute name and is written as
     * {@code @Parameters(String = {"userName"})}.
     */
    String[] String() default {};

    /**
     * Initial values of assembled {@code int} properties. Each value {@code n}
     * becomes a field named {@code int_n} (or {@code int_n_2}, {@code int_n_3},
     * … on collision) initialized to {@code n}.
     */
    int[] int_() default {};
}
