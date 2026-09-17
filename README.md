# parameters

This project is named **parameters**.

It was previously called **branch-kit** and has been renamed throughout the repository.

## Java `@Parameters`

Compile-time property assembly for PO/VO classes. Compatible with Lombok.

Supports every Java field type: primitives, boxed types, arrays, {@code String},
and custom classes. Instance fields already declared on the annotated type are
merged into the generated {@code <Type>__Parameters} superclass together with
the annotation properties.

```java
import com.pojo.parameters.Parameter;
import com.pojo.parameters.Parameters;
import lombok.Data;

@Data
@Parameters(
    String = {"userName"},
    int_ = {1, 2, 3},
    boolean_ = {true},
    type = Address.class,
    value = {
        @Parameter(name = "id", type = Long.class),
        @Parameter(name = "tags", type = String[].class)
    }
)
public class UserVO extends UserVO__Parameters {
    private String extra; // also merged into UserVO__Parameters
}
```

The processor generates `UserVO__Parameters` with JavaBean accessors (and
`equals` / `hashCode` / `toString`) for every assembled property.

Lombok annotations on the subclass keep working. Put
`@EqualsAndHashCode(callSuper = true)` and `@ToString(callSuper = true)` on the
subclass if Lombok should include the assembled properties.

## Usage

```js
const { name } = require('parameters');
console.log(name); // "parameters"
```
