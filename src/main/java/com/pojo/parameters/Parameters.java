package com.pojo.parameters;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Parameters {

    String[] String() default {};

    String[] Boolean() default {};

    String[] Byte() default {};

    String[] Short() default {};

    String[] Integer() default {};

    String[] Long() default {};

    String[] Character() default {};

    String[] Float() default {};

    String[] Double() default {};

    String[] boolean_() default {};

    String[] byte_() default {};

    String[] short_() default {};

    String[] int_() default {};

    String[] long_() default {};

    String[] char_() default {};

    String[] float_() default {};

    String[] double_() default {};

    Of[] of() default {};

    @Documented
    @Target({})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Of {

        /**
         * Erased class of the property. Use {@link #typeArgs()} for {@code List<String>}
         * and {@code Map<K, V>}. Nested generics that a class literal cannot express
         * belong in {@link #type()} instead.
         */
        Class<?> Class() default void.class;

        /**
         * Full field type source, for nested generics, wildcards, or other types that
         * {@link #Class()} plus {@link #typeArgs()} cannot write. Example:
         * {@code "java.util.List<java.util.Map<String, Address>>"}.
         */
        String type() default "";

        String[] names() default {};

        /**
         * Generic arguments for {@link #Class()}. {@code List.class} plus
         * {@code String.class} becomes {@code List<String>}; {@code Map.class} plus
         * {@code String.class, Address.class} becomes {@code Map<String, Address>}.
         */
        Class<?>[] typeArgs() default {};

        /**
         * Java initializer copied onto the generated field, for example {@code "86"},
         * {@code "\"ACTIVE\""}, or {@code "java.util.Collections.emptyList()"}.
         */
        String initializer() default "";

        /**
         * Marker (or all-defaults) annotations copied onto the generated field.
         * Annotations that need attributes should be declared on an instance field;
         * those mirrors are copied onto the generated field as well.
         */
        Class<? extends Annotation>[] annotations() default {};
    }
}
