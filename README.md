# parameters

This project is named **parameters**.

It was previously called **branch-kit** and has been renamed throughout the repository.

## Java `@Parameters`

Compile-time property assembly for PO/VO classes. Compatible with Lombok.

```java
import com.pojo.parameters.Parameters;
import lombok.Data;

@Data
@Parameters(String = {"userName"}, int_ = {1, 2, 3})
public class UserVO extends UserVO__Parameters {
}
```

The processor generates `UserVO__Parameters` with:

- `private String userName` plus `getUserName()` / `setUserName(String)`
- `private int int_1 = 1`, `int_2 = 2`, `int_3 = 3` plus matching accessors

Lombok annotations on the subclass keep working for fields declared there. Assembled properties live on the generated superclass, so the two processors do not create duplicate getters or constructors.

## Usage

```js
const { name } = require('parameters');
console.log(name); // "parameters"
```
