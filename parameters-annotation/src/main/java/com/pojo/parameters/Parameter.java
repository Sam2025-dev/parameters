package com.pojo.parameters;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A single assembled property of any type: primitive, boxed, array, or custom class.
 *
 * <pre>{@code
 * @Parameter(name = "id", type = Long.class)
 * @Parameter(name = "homeAddress", type = Address.class)
 * @Parameter(name = "tags", type = String[].class)
 * @Parameter(name = "count", type = int.class)
 * }</pre>
 */
@Documented
@Target({})
@Retention(RetentionPolicy.SOURCE)
public @interface Parameter {

    /**
     * Java identifier used as the field name.
     */
    String name();

    /**
     * Field type. Primitives use {@code int.class}, {@code boolean.class}, and so on;
     * custom types use the class literal.
     */
    Class<?> type();
}
