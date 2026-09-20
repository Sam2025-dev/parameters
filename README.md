# parameters

Compile-time `@Parameters` for PO/VO classes. Compatible with Lombok and Java 8.

Place `@Parameters` on a class and extend the generated `<SimpleName>__Parameters`
superclass. The processor assembles annotation properties and existing instance
fields into that superclass with JavaBean accessors, `equals`, `hashCode`, and
`toString`.

Published on GitHub Packages as `com.github.parameters:parameters:0.1-SNAPSHOT`.

```xml
<dependency>
    <groupId>com.github.parameters</groupId>
    <artifactId>parameters</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>
```

Add the GitHub Packages repository (snapshots must be enabled):

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Sam2025-dev/parameters</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

GitHub Packages requires a token even for a public package. In `~/.m2/settings.xml` the `<id>` must match `github`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_GITHUB_PAT</password>
        </server>
    </servers>
</settings>
```

The PAT needs at least `read:packages`. Package page:
https://github.com/Sam2025-dev/parameters/packages/3259824

## Maven Central

Maven Central cannot host `com.github.parameters` unless that DNS namespace is verified.
Signing in to [Central Portal](https://central.sonatype.com/) with GitHub grants
`io.github.sam2025-dev`. After a `v0.1` release, consumers can use Central with
no extra repository:

```xml
<dependency>
    <groupId>io.github.sam2025-dev</groupId>
    <artifactId>parameters</artifactId>
    <version>0.1</version>
</dependency>
```

Publishing to Central needs these GitHub Actions secrets:

| Secret | Value |
| --- | --- |
| `CENTRAL_USERNAME` | User token username from https://central.sonatype.com/account |
| `CENTRAL_PASSWORD` | User token password |
| `GPG_PRIVATE_KEY` | Exported secret key (`gpg --armor --export-secret-keys`) |
| `GPG_PASSPHRASE` | Passphrase for that key |

Upload the matching public key to a keyserver (for example https://keys.openpgp.org).
Then tag `v0.1` or run the **Publish** workflow manually.

Register it as an annotation processor next to Lombok:

```xml
<annotationProcessorPaths>
    <path>
        <groupId>com.github.parameters</groupId>
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
| `of` | any Java type, via `@Of` |

When `@Of` omits `names`, the field is the decapitalized simple class name
(`Address` → `address`). Types whose simple name is not a valid identifier
(for example `Long` → `long`) must declare `names`.

`@Of` is also how defaults, field annotations, `List`/`Map`, and generics are
declared. Class literals erase type arguments, so those cases need the extra
members below.

| `@Of` member | Role |
| --- | --- |
| `Class` | Erased class of the field (`Address.class`, `List.class`, `int.class`) |
| `typeArgs` | Generic arguments for `Class` (`List<String>`, `Map<String, Address>`) |
| `type` | Full type source when class literals cannot write nested generics |
| `names` | One or more field names; derived from the type when omitted |
| `initializer` | Java expression copied onto the generated field |
| `annotations` | Marker (or all-defaults) annotations copied onto the generated field |

`Class` and `type` are alternatives. `type` and `typeArgs` cannot be combined:
put nested generics in `type`.

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
}
```

This generates `UserVO__Parameters` with:

- `String userName`, `Boolean enabledFlag`, `Integer id`, `Long userId`
- `int countryCode`, `int cityCode`, `int areaCode`
- `Address homeAddress`, `String[] tags`

Lombok annotations on the subclass keep working. Use
`@EqualsAndHashCode(callSuper = true)` and `@ToString(callSuper = true)` if
Lombok should include the assembled properties.

Shorthand members (`String`, `int_`, …) only supply names and types. Defaults,
annotations, and generic collections go on `@Of`, as in the next sections.

## Defaults

Generated fields use ordinary Java defaults (`null`, `0`, `false`) unless you
set an initializer.

Per-name defaults are not available on shorthand members. Put them on `@Of`:

```java
@Of(Class = String.class, names = {"status"}, initializer = "\"ACTIVE\""),
@Of(Class = int.class, names = {"countryCode"}, initializer = "86"),
@Of(
    Class = List.class,
    typeArgs = {String.class},
    names = {"roles"},
    initializer = "java.util.Collections.emptyList()")
```

`initializer` is copied as Java source, so strings need escaped quotes
(`"\"ACTIVE\""`), longs need `L`, and collection factories should be
fully-qualified if you do not add imports to the generated superclass.

Instance field initializers are copied when the compiler exposes the source
(javac does), including compile-time constants:

```java
@Parameters
public class UserVO extends UserVO__Parameters {
    private int countryCode = 86;
    private String status = "ACTIVE";
    private List<String> roles = java.util.Collections.emptyList();
}
```

Simple names in those expressions must already be fully qualified, because the
generated superclass does not reuse the original file's imports. You can also
assign defaults in a subclass constructor after generation.

## Annotations

Marker annotations (and annotations whose members all have defaults) can be
listed on `@Of`:

```java
@Of(Class = String.class, names = {"userName"}, annotations = {Deprecated.class})
```

That becomes `@Deprecated private String userName;` on the generated
superclass. Bean Validation or Jackson markers work the same way when those
types are on the compile classpath:

```java
@Of(Class = String.class, names = {"email"}, annotations = {NotNull.class, Email.class})
```

Annotations that need attributes (`@Size(min = 1)`, `@JsonProperty("user_name")`)
cannot be expressed with `Class[]`. Declare the field on the model class; the
processor copies field annotation mirrors—including member values—onto the
generated field:

```java
@Parameters
public class UserVO extends UserVO__Parameters {
    @Size(min = 1, max = 32)
    @JsonProperty("user_name")
    private String userName;
}
```

Lombok annotations are not copied. Prefer declaring properties on `@Parameters`
when the class also uses `@Data`, so accessors live on the generated superclass
instead of a second field on the subclass.

## Collections, maps, and generics

`@Of(Class = List.class)` generates a raw `List`. Pass `typeArgs` for the
element or key/value types:

```java
@Of(Class = List.class, typeArgs = {String.class}, names = {"roles"}),
@Of(Class = Map.class, typeArgs = {String.class, Address.class}, names = {"addresses"})
```

This generates `java.util.List<String> roles` and
`java.util.Map<String, Address> addresses`. `typeArgs` length must match the
type parameters (`List` 1, `Map` 2).

Class literals cannot write nested generics such as `List<Map<String, Address>>`
or wildcards. Use `type` for those:

```java
@Of(type = "java.util.List<java.util.Map<String, Address>>", names = {"grouped"}),
@Of(type = "java.util.List<? extends Address>", names = {"homes"})
```

Instance fields keep their generic types when merged, so this is equivalent for
`List`/`Map`:

```java
@Parameters
public class UserVO extends UserVO__Parameters {
    private List<String> roles;
    private Map<String, Address> addresses;
}
```

## Richer model

One class that uses the pieces together:

```java
import com.pojo.parameters.Parameters;
import com.pojo.parameters.Parameters.Of;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Parameters(
    String = {"userName"},
    int_ = {"countryCode"},
    of = {
        @Of(Class = Address.class, names = {"homeAddress"}),
        @Of(
            Class = List.class,
            typeArgs = {String.class},
            names = {"roles"},
            initializer = "java.util.Collections.emptyList()"),
        @Of(
            Class = Map.class,
            typeArgs = {String.class, Address.class},
            names = {"addresses"}),
        @Of(
            type = "java.util.List<java.util.Map<String, String>>",
            names = {"attributes"}),
        @Of(
            Class = String.class,
            names = {"status"},
            initializer = "\"ACTIVE\"",
            annotations = {Deprecated.class})
    }
)
public class ProfileVO extends ProfileVO__Parameters {
}
```

Generated `ProfileVO__Parameters` includes:

- `String userName`, `int countryCode`
- `Address homeAddress`
- `List<String> roles = java.util.Collections.emptyList()`
- `Map<String, Address> addresses`
- `List<Map<String, String>> attributes`
- `@Deprecated String status = "ACTIVE"`
