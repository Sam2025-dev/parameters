# parameters

Compile-time `@Data` for PO/VO classes, implemented like Lombok: members are
injected into the annotated class. No superclass is generated.

Place `com.pojo.parameters.Data` on a class. The processor adds `@Of` fields
(if missing) and JavaBean accessors, `equals`, `hashCode`, and `toString` for
those fields and existing instance fields.

Published on GitHub Packages as `com.github.parameters:parameters:1.0`.

```xml
<dependency>
    <groupId>com.github.parameters</groupId>
    <artifactId>parameters</artifactId>
    <version>1.0</version>
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

GitHub Packages requires a token even for a public package. In `~/.m2/settings.xml`
the `<id>` must match `github`:

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

Register it as an annotation processor next to Lombok:

```xml
<annotationProcessorPaths>
    <path>
        <groupId>com.github.parameters</groupId>
        <artifactId>parameters</artifactId>
        <version>1.0</version>
    </path>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
</annotationProcessorPaths>
```

## Maven Central

Maven Central cannot host `com.github.parameters` unless that DNS namespace is verified.
Signing in to [Central Portal](https://central.sonatype.com/) with GitHub grants
`io.github.sam2025-dev`. After a `v1.0` release, consumers can use Central with
no extra repository:

```xml
<dependency>
    <groupId>io.github.sam2025-dev</groupId>
    <artifactId>parameters</artifactId>
    <version>1.0</version>
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
Then tag `v1.0` or run the **Publish** workflow manually.

## `@Data`

`@Data` has one member: `of`, an array of `@Of`.

You can also leave `of` empty and declare instance fields on the class. Those
fields get accessors on the same class. Static fields are ignored.
The annotation must still produce at least one property.

Use `com.pojo.parameters.Data`, not `lombok.Data`, when you want assembled
properties. Lombok `@Data` can still be used on other types in the same project.

## `@Of`

| Member | Role |
| --- | --- |
| `Class` | Erased class of the field (`Address.class`, `List.class`, `int.class`) |
| `type` | Full type source when class literals cannot write nested generics |
| `names` | One or more field names; derived from the type when omitted |
| `typeArgs` | Generic arguments for `Class` (`List<String>`, `Map<String, Address>`) |
| `initializer` | Java expression copied onto the generated field |

Each `@Of` must declare `Class` or `type`. `Class` and `type` are alternatives.
`type` and `typeArgs` cannot be combined: put nested generics in `type`.

When `names` is omitted, the field is the decapitalized simple class name
(`Address` → `address`). Types whose simple name is not a valid identifier
(for example `Long` → `long`) must declare `names`.

## Example

```java
import com.pojo.parameters.Data;
import com.pojo.parameters.Data.Of;

@Data(of = {
    @Of(Class = String.class, names = {"userName"}),
    @Of(Class = Boolean.class, names = {"enabledFlag"}),
    @Of(Class = Integer.class, names = {"id"}),
    @Of(Class = Long.class, names = {"userId"}),
    @Of(Class = int.class, names = {"countryCode", "cityCode", "areaCode"}),
    @Of(Class = Address.class, names = {"homeAddress"}),
    @Of(Class = String[].class, names = {"tags"})
})
public class UserVO {
}
```

This injects into `UserVO`:

- `String userName`, `Boolean enabledFlag`, `Integer id`, `Long userId`
- `int countryCode`, `int cityCode`, `int areaCode`
- `Address homeAddress`, `String[] tags`

Accessors, `equals`, `hashCode`, and `toString` live on the annotated class.

## Defaults

Generated fields use ordinary Java defaults (`null`, `0`, `false`) unless you
set an initializer on `@Of`:

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
(`"\"ACTIVE\""`), longs need `L`. Simple names follow the annotated file's
imports.

Instance field initializers stay on the class:

```java
@Data
public class UserVO {
    private int countryCode = 86;
    private String status = "ACTIVE";
    private List<String> roles = java.util.Collections.emptyList();
}
```

You can also assign defaults in a constructor.

## Annotations

Declare annotations on the model field. They stay on that field; `@Of` fields
are injected without extra annotations:

```java
@Data
public class UserVO {
    @Size(min = 1, max = 32)
    @JsonProperty("user_name")
    @Deprecated
    private String userName;
}
```

Lombok annotations are not treated specially. Prefer `@Of` for properties that
should be injected, so you do not declare the same field twice.

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
@Data
public class UserVO {
    private List<String> roles;
    private Map<String, Address> addresses;
}
```

## Richer model

One class that uses the pieces together:

```java
import com.pojo.parameters.Data;
import com.pojo.parameters.Data.Of;

import java.util.List;
import java.util.Map;

@Data(of = {
    @Of(Class = String.class, names = {"userName"}),
    @Of(Class = int.class, names = {"countryCode"}),
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
        names = {"attributes"})
})
public class ProfileVO {
    @Deprecated
    private String status = "ACTIVE";
}
```

Generated members on `ProfileVO` include:

- `String userName`, `int countryCode`
- `Address homeAddress`
- `List<String> roles = java.util.Collections.emptyList()`
- `Map<String, Address> addresses`
- `List<Map<String, String>> attributes`
- `@Deprecated String status = "ACTIVE"`
