package com.pojo.parameters;

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

        Class<?> Class();

        String[] names() default {};
    }
}
