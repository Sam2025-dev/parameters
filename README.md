# parameters

This project is named **parameters**.

It was previously called **branch-kit** and has been renamed throughout the repository.

## Java `@Parameters`

Compile-time property assembly for PO/VO classes. Compatible with Lombok.

Supports every Java field type: primitives, boxed types, arrays, `String`,
and custom classes. Instance fields already declared on the annotated type are
merged into the generated `<Type>__Parameters` superclass together with
the annotation properties.

`int_` and `long_` are primitive Java properties (initialized fields such as
`int_1` and `long_18`). `of` declares a class plus `String[] names` for that class.

```java
import com.pojo.parameters.Of;
import com.pojo.parameters.Parameters;
import lombok.Data;

@Data
@Parameters(
    String = {"userName"},
    int_ = {1, 2, 3},
    long_ = {18L},
    of = {
        @Of(Class = Address.class, names = {"homeAddress"}),
        @Of(Class = Long.class, names = {"id"}),
        @Of(Class = String[].class, names = {"tags"})
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
