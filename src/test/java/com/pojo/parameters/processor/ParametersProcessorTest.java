package com.pojo.parameters.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class ParametersProcessorTest {

    @Test
    void injectsAccessorsOnTheAnnotatedClass() {
        Compilation compilation = compile(
                type("com.example.DemoVO",
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Data;",
                        "import com.pojo.parameters.Data.Of;",
                        "",
                        "@Data(of = {",
                        "        @Of(Class = String.class, names = {\"userName\"}),",
                        "        @Of(Class = int.class, names = {\"countryCode\", \"cityCode\", \"areaCode\"})",
                        "})",
                        "public class DemoVO {",
                        "}"),
                use("com.example.DemoVO",
                        "v.setUserName(\"a\");",
                        "String n = v.getUserName();",
                        "v.setCountryCode(86);",
                        "int c = v.getCountryCode();"));
        assertThat(compilation).succeeded();
    }

    @Test
    void injectsAllPrimitiveAndBoxedTypesViaOf() {
        Compilation compilation = compile(
                JavaFileObjects.forSourceString(
                        "com.example.Address",
                        src("package com.example;", "", "public class Address { public String city; }")),
                type("com.example.AllTypesVO",
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Data;",
                        "import com.pojo.parameters.Data.Of;",
                        "",
                        "@Data(of = {",
                        "        @Of(Class = String.class, names = {\"userName\"}),",
                        "        @Of(Class = Boolean.class, names = {\"enabledFlag\"}),",
                        "        @Of(Class = boolean.class, names = {\"enabled\"}),",
                        "        @Of(Class = int.class, names = {\"count\"}),",
                        "        @Of(Class = Address.class),",
                        "        @Of(Class = String[].class, names = {\"tags\"})",
                        "})",
                        "public class AllTypesVO {",
                        "}"),
                use("com.example.AllTypesVO",
                        "v.setEnabled(true);",
                        "boolean e = v.isEnabled();",
                        "v.setTags(new String[]{\"a\"});",
                        "String[] t = v.getTags();",
                        "com.example.Address a = v.getAddress();"));
        assertThat(compilation).succeeded();
    }

    @Test
    void generatesAccessorsForExistingFields() {
        Compilation compilation = compile(
                type("com.example.MergeVO",
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Data;",
                        "import com.pojo.parameters.Data.Of;",
                        "",
                        "@Data(of = @Of(Class = String.class, names = {\"userName\"}))",
                        "public class MergeVO {",
                        "    private Long id;",
                        "}"),
                use("com.example.MergeVO",
                        "v.setId(1L);",
                        "Long id = v.getId();",
                        "v.setUserName(\"n\");"));
        assertThat(compilation).succeeded();
    }

    @Test
    void emptyAnnotationStillUsesExistingFields() {
        Compilation compilation = compile(
                type("com.example.OnlyFieldsVO",
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Data;",
                        "",
                        "@Data",
                        "public class OnlyFieldsVO {",
                        "    private String title;",
                        "}"),
                use("com.example.OnlyFieldsVO",
                        "v.setTitle(\"t\");",
                        "String t = v.getTitle();"));
        assertThat(compilation).succeeded();
    }

    @Test
    void rejectsEmptyData() {
        Compilation compilation = compile(type("com.example.EmptyVO",
                "package com.example;",
                "",
                "import com.pojo.parameters.Data;",
                "",
                "@Data",
                "public class EmptyVO {",
                "}"));
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("must declare at least one property or instance field to merge");
    }

    @Test
    void rejectsInvalidPropertyName() {
        Compilation compilation = compile(type("com.example.BadVO",
                "package com.example;",
                "",
                "import com.pojo.parameters.Data;",
                "import com.pojo.parameters.Data.Of;",
                "",
                "@Data(of = @Of(Class = String.class, names = {\"123oops\"}))",
                "public class BadVO {",
                "}"));
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Invalid @Of property name");
    }

    @Test
    void mergesSameNameSameTypeInsteadOfFailing() {
        Compilation compilation = compile(
                type("com.example.DupVO",
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Data;",
                        "import com.pojo.parameters.Data.Of;",
                        "",
                        "@Data(of = @Of(Class = String.class, names = {\"userName\"}))",
                        "public class DupVO {",
                        "    private String userName;",
                        "}"),
                use("com.example.DupVO", "v.setUserName(\"n\");"));
        assertThat(compilation).succeeded();
    }

    @Test
    void rejectsConflictingTypesForTheSameName() {
        Compilation compilation = compile(type("com.example.ConflictVO",
                "package com.example;",
                "",
                "import com.pojo.parameters.Data;",
                "import com.pojo.parameters.Data.Of;",
                "",
                "@Data(of = @Of(Class = Integer.class, names = {\"id\"}))",
                "public class ConflictVO {",
                "    private Long id;",
                "}"));
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Duplicate property name with conflicting types: id");
    }

    @Test
    void ofWithoutNamesUsesDecapitalizedClassName() {
        Compilation compilation = compile(
                JavaFileObjects.forSourceString(
                        "com.example.Address",
                        src("package com.example;", "", "public class Address {}")),
                type("com.example.DerivedNameVO",
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Data;",
                        "import com.pojo.parameters.Data.Of;",
                        "",
                        "@Data(of = @Of(Class = Address.class))",
                        "public class DerivedNameVO {",
                        "}"),
                use("com.example.DerivedNameVO", "com.example.Address a = v.getAddress();"));
        assertThat(compilation).succeeded();
    }

    @Test
    void ofRequiresNamesWhenClassSimpleNameIsNotAnIdentifier() {
        Compilation compilation = compile(type("com.example.BadOfVO",
                "package com.example;",
                "",
                "import com.pojo.parameters.Data;",
                "import com.pojo.parameters.Data.Of;",
                "",
                "@Data(of = @Of(Class = Long.class))",
                "public class BadOfVO {",
                "}"));
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("declare names explicitly on @Of");
    }

    @Test
    void ofSupportsListMapAndNestedGenerics() {
        Compilation compilation = compile(
                JavaFileObjects.forSourceString(
                        "com.example.Address",
                        src("package com.example;", "", "public class Address {}")),
                type("com.example.RichVO",
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Data;",
                        "import com.pojo.parameters.Data.Of;",
                        "import java.util.List;",
                        "import java.util.Map;",
                        "",
                        "@Data(of = {",
                        "        @Of(Class = List.class, typeArgs = {String.class}, names = {\"roles\"},",
                        "                initializer = \"java.util.Collections.emptyList()\"),",
                        "        @Of(Class = Map.class, typeArgs = {String.class, Address.class}, names = {\"addresses\"}),",
                        "        @Of(type = \"java.util.List<java.util.Map<String, String>>\", names = {\"attributes\"})",
                        "})",
                        "public class RichVO {",
                        "}"),
                use("com.example.RichVO",
                        "java.util.List<String> r = v.getRoles();",
                        "java.util.Map<String, com.example.Address> a = v.getAddresses();",
                        "java.util.List<java.util.Map<String, String>> x = v.getAttributes();"));
        assertThat(compilation).succeeded();
    }

    @Test
    void ofRejectsTypeArgsArityMismatch() {
        Compilation compilation = compile(type("com.example.ArityVO",
                "package com.example;",
                "",
                "import com.pojo.parameters.Data;",
                "import com.pojo.parameters.Data.Of;",
                "import java.util.List;",
                "",
                "@Data(of = @Of(Class = List.class, typeArgs = {String.class, Integer.class}, names = {\"roles\"}))",
                "public class ArityVO {",
                "}"));
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("expects 1 type argument(s), not 2");
    }

    @Test
    void ofRejectsTypeCombinedWithTypeArgs() {
        Compilation compilation = compile(type("com.example.CombinedVO",
                "package com.example;",
                "",
                "import com.pojo.parameters.Data;",
                "import com.pojo.parameters.Data.Of;",
                "import java.util.List;",
                "",
                "@Data(of = @Of(Class = List.class, typeArgs = {String.class},",
                "        type = \"java.util.List<String>\", names = {\"roles\"}))",
                "public class CombinedVO {",
                "}"));
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("@Of type and typeArgs cannot be combined");
    }

    @Test
    void ofRequiresClassOrType() {
        Compilation compilation = compile(type("com.example.EmptyOfVO",
                "package com.example;",
                "",
                "import com.pojo.parameters.Data;",
                "import com.pojo.parameters.Data.Of;",
                "",
                "@Data(of = @Of(names = {\"value\"}))",
                "public class EmptyOfVO {",
                "}"));
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Each @Of must declare Class or type");
    }

    private static Compilation compile(JavaFileObject... sources) {
        return javac()
                .withProcessors(new ParametersProcessor())
                .compile(sources);
    }

    private static JavaFileObject type(String fqn, String... lines) {
        return JavaFileObjects.forSourceString(fqn, src(lines));
    }

    private static JavaFileObject use(String fqn, String... statements) {
        int lastDot = fqn.lastIndexOf('.');
        String pkg = fqn.substring(0, lastDot);
        String simple = fqn.substring(lastDot + 1);
        StringBuilder body = new StringBuilder();
        body.append("package ").append(pkg).append(";\n");
        body.append("class Use {\n  void t(").append(simple).append(" v) {\n");
        for (String statement : statements) {
            body.append("    ").append(statement).append('\n');
        }
        body.append("  }\n}\n");
        return JavaFileObjects.forSourceString(pkg + ".Use", body.toString());
    }

    private static String src(String... lines) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                text.append('\n');
            }
            text.append(lines[i]);
        }
        return text.toString();
    }
}
