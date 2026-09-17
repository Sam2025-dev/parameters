# parameters

Compile-time `@Parameters` for PO/VO classes. Compatible with Lombok.

Place `@Parameters` on a class and extend the generated `<SimpleName>__Parameters`
superclass. The processor assembles annotation properties and existing instance
fields into that superclass with JavaBean accessors, `equals`, `hashCode`, and
`toString`.

Artifact: `com.pojo:parameters:0.1-SNAPSHOT`

```xml
<dependency>
    <groupId>com.pojo</groupId>
    <artifactId>parameters</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>
```

Register it as an annotation processor next to Lombok:

```xml
<annotationProcessorPaths>
    <path>
        <groupId>com.pojo</groupId>
        <artifactId>parameters</artifactId>
        <version>0.1-SNAPSHOT</version>
    </path>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
</annotationProcessorPaths>
```

## Members

Every member takes property **names**. Primitive members use a trailing `_`
because `int` and `boolean` are Java keywords.

| Member | Generated type |
| --- | --- |
| `String` | `String` |
| `Boolean` `Byte` `Short` `Integer` `Long` `Character` `Float` `Double` | boxed types |
| `boolean_` `byte_` `short_` `int_` `long_` `char_` `float_` `double_` | primitives |
| `of` | any Java class, via `@Of(Class, names)` |

When `@Of` omits `names`, the field is the decapitalized simple class name
(`Address` → `address`). Types whose simple name is not a valid identifier
(for example `Long` → `long`) must declare `names`.

## Example

```java
import com.pojo.parameters.Parameters;
import com.pojo.parameters.Parameters.Of;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Parameters(
    String = {"userName"},
    Boolean = {"enabledFlag"},
    Integer = {"id"},
    Long = {"userId"},
    int_ = {"countryCode", "cityCode", "areaCode"},
    of = {
        @Of(Class = Address.class, names = {"homeAddress"}),
        @Of(Class = String[].class, names = {"tags"})
    }
)
public class UserVO extends UserVO__Parameters {
    private String extra;
}
```

This generates `UserVO__Parameters` with:

- `String userName`, `Boolean enabledFlag`, `Integer id`, `Long userId`
- `int countryCode`, `int cityCode`, `int areaCode`
- `Address homeAddress`, `String[] tags`
- `String extra` (merged from the subclass)

Lombok annotations on the subclass keep working. Use
`@EqualsAndHashCode(callSuper = true)` and `@ToString(callSuper = true)` if
Lombok should include the assembled properties.
