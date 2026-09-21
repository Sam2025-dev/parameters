package com.pojo.parameters.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class ParametersProcessorTest {

    @Test
    void generatesSuperclassWithStringAndIntProperties() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.DemoVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters(String = {\"userName\"}, int_ = {\"countryCode\", \"cityCode\", \"areaCode\"})",
                        "public class DemoVO extends DemoVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.DemoVO__Parameters")
                .contentsAsUtf8String()
                .contains("private String userName;");
        assertThat(compilation)
                .generatedSourceFile("com.example.DemoVO__Parameters")
                .contentsAsUtf8String()
                .contains("private int countryCode;");
        assertThat(compilation)
                .generatedSourceFile("com.example.DemoVO__Parameters")
                .contentsAsUtf8String()
                .contains("private int cityCode;");
        assertThat(compilation)
                .generatedSourceFile("com.example.DemoVO__Parameters")
                .contentsAsUtf8String()
                .contains("private int areaCode;");
        assertThat(compilation)
                .generatedSourceFile("com.example.DemoVO__Parameters")
                .contentsAsUtf8String()
                .contains("public String getUserName()");
        assertThat(compilation)
                .generatedSourceFile("com.example.DemoVO__Parameters")
                .contentsAsUtf8String()
                .contains("public void setUserName(String userName)");
        assertThat(compilation)
                .generatedSourceFile("com.example.DemoVO__Parameters")
                .contentsAsUtf8String()
                .contains("public int getCountryCode()");
    }

    @Test
    void generatesAllPrimitiveShorthandsAndCustomTypes() {
        JavaFileObject address = JavaFileObjects.forSourceString(
                "com.example.Address",
                src(
                        "package com.example;",
                        "",
                        "public class Address {",
                        "    public String city;",
                        "}"));
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.AllTypesVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import com.pojo.parameters.Parameters.Of;",
                        "",
                        "@Parameters(",
                        "        String = {\"userName\"},",
                        "        Boolean = {\"enabledFlag\"},",
                        "        Byte = {\"levelBox\"},",
                        "        Short = {\"rankBox\"},",
                        "        Integer = {\"countBox\"},",
                        "        Long = {\"id\"},",
                        "        Character = {\"gradeBox\"},",
                        "        Float = {\"scoreBox\"},",
                        "        Double = {\"amountBox\"},",
                        "        boolean_ = {\"enabled\"},",
                        "        byte_ = {\"level\"},",
                        "        short_ = {\"rank\"},",
                        "        int_ = {\"count\"},",
                        "        long_ = {\"age\"},",
                        "        char_ = {\"grade\"},",
                        "        float_ = {\"score\"},",
                        "        double_ = {\"amount\"},",
                        "        of = {",
                        "                @Of(Class = Address.class),",
                        "                @Of(Class = String[].class, names = {\"tags\"})",
                        "        }",
                        ")",
                        "public class AllTypesVO extends AllTypesVO__Parameters {",
                        "}"));

        Compilation compilation = compile(address, source);
        assertThat(compilation).succeeded();
        com.google.common.truth.StringSubject generated = assertThat(compilation)
                .generatedSourceFile("com.example.AllTypesVO__Parameters")
                .contentsAsUtf8String();
        generated.contains("private String userName;");
        generated.contains("private Boolean enabledFlag;");
        generated.contains("private Byte levelBox;");
        generated.contains("private Short rankBox;");
        generated.contains("private Integer countBox;");
        generated.contains("private Long id;");
        generated.contains("private Character gradeBox;");
        generated.contains("private Float scoreBox;");
        generated.contains("private Double amountBox;");
        generated.contains("private boolean enabled;");
        generated.contains("private byte level;");
        generated.contains("private short rank;");
        generated.contains("private int count;");
        generated.contains("private long age;");
        generated.contains("private char grade;");
        generated.contains("private float score;");
        generated.contains("private double amount;");
        generated.contains("private com.example.Address address;");
        generated.contains("private String[] tags;");
        generated.contains("public boolean isEnabled()");
        generated.contains("public com.example.Address getAddress()");
    }

    @Test
    void mergesInstanceFieldsIntoGeneratedParameters() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.MergeVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters(String = {\"userName\"})",
                        "public class MergeVO extends MergeVO__Parameters {",
                        "    private Long id;",
                        "    private String label;",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.MergeVO__Parameters")
                .contentsAsUtf8String()
                .contains("private Long id;");
        assertThat(compilation)
                .generatedSourceFile("com.example.MergeVO__Parameters")
                .contentsAsUtf8String()
                .contains("private String label;");
        assertThat(compilation)
                .generatedSourceFile("com.example.MergeVO__Parameters")
                .contentsAsUtf8String()
                .contains("private String userName;");
    }

    @Test
    void emptyAnnotationStillMergesExistingFields() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.OnlyFieldsVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters",
                        "public class OnlyFieldsVO extends OnlyFieldsVO__Parameters {",
                        "    private String title;",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.OnlyFieldsVO__Parameters")
                .contentsAsUtf8String()
                .contains("private String title;");
    }

    @Test
    void rejectsEmptyParameters() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.EmptyVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters",
                        "public class EmptyVO extends EmptyVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("must declare at least one property or instance field to merge");
    }

    @Test
    void rejectsInvalidPropertyName() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.BadVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters(String = {\"123oops\"})",
                        "public class BadVO extends BadVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Invalid String property name");
    }

    @Test
    void rejectsInvalidIntPropertyName() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.BadIntVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters(int_ = {\"123oops\"})",
                        "public class BadIntVO extends BadIntVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Invalid int property name");
    }

    @Test
    void mergesSameNameSameTypeInsteadOfFailing() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.DupVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters(String = {\"userName\"})",
                        "public class DupVO extends DupVO__Parameters {",
                        "    private String userName;",
                        "}"));

        Compilation compilation = compile(source);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.DupVO__Parameters")
                .contentsAsUtf8String()
                .contains("private String userName;");
    }

    @Test
    void rejectsConflictingTypesForTheSameName() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.ConflictVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import com.pojo.parameters.Parameters.Of;",
                        "",
                        "@Parameters(of = @Of(Class = Integer.class, names = {\"id\"}))",
                        "public class ConflictVO extends ConflictVO__Parameters {",
                        "    private Long id;",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Duplicate property name with conflicting types: id");
    }

    @Test
    void ofDeclaresClassAndMultipleNames() {
        JavaFileObject address = JavaFileObjects.forSourceString(
                "com.example.Address",
                src(
                        "package com.example;",
                        "",
                        "public class Address {",
                        "    public String city;",
                        "}"));
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.OfVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import com.pojo.parameters.Parameters.Of;",
                        "",
                        "@Parameters(",
                        "        int_ = {\"countryCode\"},",
                        "        long_ = {\"age\"},",
                        "        of = @Of(Class = Address.class, names = {\"homeAddress\", \"workAddress\"})",
                        ")",
                        "public class OfVO extends OfVO__Parameters {",
                        "}"));

        Compilation compilation = compile(address, source);
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.OfVO__Parameters")
                .contentsAsUtf8String()
                .contains("private int countryCode;");
        assertThat(compilation)
                .generatedSourceFile("com.example.OfVO__Parameters")
                .contentsAsUtf8String()
                .contains("private long age;");
        assertThat(compilation)
                .generatedSourceFile("com.example.OfVO__Parameters")
                .contentsAsUtf8String()
                .contains("private com.example.Address homeAddress;");
        assertThat(compilation)
                .generatedSourceFile("com.example.OfVO__Parameters")
                .contentsAsUtf8String()
                .contains("private com.example.Address workAddress;");
        assertThat(compilation)
                .generatedSourceFile("com.example.OfVO__Parameters")
                .contentsAsUtf8String()
                .contains("public com.example.Address getHomeAddress()");
        assertThat(compilation)
                .generatedSourceFile("com.example.OfVO__Parameters")
                .contentsAsUtf8String()
                .contains("public void setWorkAddress(com.example.Address workAddress)");
    }

    @Test
    void ofWithoutNamesUsesDecapitalizedClassName() {
        JavaFileObject address = JavaFileObjects.forSourceString(
                "com.example.Address",
                src(
                        "package com.example;",
                        "",
                        "public class Address {}"));
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.DerivedNameVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import com.pojo.parameters.Parameters.Of;",
                        "",
                        "@Parameters(of = @Of(Class = Address.class))",
                        "public class DerivedNameVO extends DerivedNameVO__Parameters {",
                        "}"));

        Compilation compilation = compile(address, source);
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.DerivedNameVO__Parameters")
                .contentsAsUtf8String()
                .contains("private com.example.Address address;");
    }

    @Test
    void ofRequiresNamesWhenClassSimpleNameIsNotAnIdentifier() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.BadOfVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import com.pojo.parameters.Parameters.Of;",
                        "",
                        "@Parameters(of = @Of(Class = Long.class))",
                        "public class BadOfVO extends BadOfVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("declare names explicitly on @Of");
    }

    @Test
    void rejectsInvalidOfPropertyName() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.BadNameVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import com.pojo.parameters.Parameters.Of;",
                        "",
                        "@Parameters(of = @Of(Class = Long.class, names = {\"123id\"}))",
                        "public class BadNameVO extends BadNameVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Invalid @Of property name");
    }

    @Test
    void ofSupportsListMapDefaultsAndMarkerAnnotations() {
        JavaFileObject address = JavaFileObjects.forSourceString(
                "com.example.Address",
                src(
                        "package com.example;",
                        "",
                        "public class Address {",
                        "    public String city;",
                        "}"));
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.RichVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import com.pojo.parameters.Parameters.Of;",
                        "import java.util.List;",
                        "import java.util.Map;",
                        "",
                        "@Parameters(of = {",
                        "        @Of(Class = List.class, typeArgs = {String.class}, names = {\"roles\"},",
                        "                initializer = \"java.util.Collections.emptyList()\"),",
                        "        @Of(Class = Map.class, typeArgs = {String.class, Address.class}, names = {\"addresses\"}),",
                        "        @Of(Class = String.class, names = {\"status\"}, initializer = \"\\\"ACTIVE\\\"\")",
                        "})",
                        "public class RichVO extends RichVO__Parameters {",
                        "}"));

        Compilation compilation = compile(address, source);
        assertThat(compilation).succeeded();
        com.google.common.truth.StringSubject generated = assertThat(compilation)
                .generatedSourceFile("com.example.RichVO__Parameters")
                .contentsAsUtf8String();
        generated.contains("private java.util.List<String> roles = java.util.Collections.emptyList();");
        generated.contains("private java.util.Map<String, com.example.Address> addresses;");
        generated.contains("private String status = \"ACTIVE\";");
    }

    @Test
    void ofSupportsNestedGenericTypeSource() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.NestedVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import com.pojo.parameters.Parameters.Of;",
                        "",
                        "@Parameters(of = @Of(",
                        "        type = \"java.util.List<java.util.Map<String, String>>\",",
                        "        names = {\"attributes\"}))",
                        "public class NestedVO extends NestedVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.NestedVO__Parameters")
                .contentsAsUtf8String()
                .contains("private java.util.List<java.util.Map<String, String>> attributes;");
    }

    @Test
    void ofRejectsTypeArgsArityMismatch() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.ArityVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import com.pojo.parameters.Parameters.Of;",
                        "import java.util.List;",
                        "",
                        "@Parameters(of = @Of(Class = List.class, typeArgs = {String.class, Integer.class}, names = {\"roles\"}))",
                        "public class ArityVO extends ArityVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("expects 1 type argument(s), not 2");
    }

    @Test
    void ofRejectsTypeArgsOnNonGenericClass() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.RawArgsVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import com.pojo.parameters.Parameters.Of;",
                        "",
                        "@Parameters(of = @Of(Class = String.class, typeArgs = {Integer.class}, names = {\"label\"}))",
                        "public class RawArgsVO extends RawArgsVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("typeArgs can only be used with generic types");
    }

    @Test
    void ofRejectsTypeCombinedWithTypeArgs() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.CombinedVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import com.pojo.parameters.Parameters.Of;",
                        "import java.util.List;",
                        "",
                        "@Parameters(of = @Of(Class = List.class, typeArgs = {String.class},",
                        "        type = \"java.util.List<String>\", names = {\"roles\"}))",
                        "public class CombinedVO extends CombinedVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("@Of type and typeArgs cannot be combined");
    }

    @Test
    void parametersRemoveAnnotDropsCopiedAnnotationsFromMemberFields() {
        JavaFileObject label = JavaFileObjects.forSourceString(
                "com.example.Label",
                src(
                        "package com.example;",
                        "",
                        "import java.lang.annotation.ElementType;",
                        "import java.lang.annotation.Retention;",
                        "import java.lang.annotation.RetentionPolicy;",
                        "import java.lang.annotation.Target;",
                        "",
                        "@Target(ElementType.FIELD)",
                        "@Retention(RetentionPolicy.SOURCE)",
                        "public @interface Label {",
                        "    String value();",
                        "    int max() default 0;",
                        "}"));
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.ClassStripVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters(removeAnnot = {Deprecated.class, Label.class})",
                        "public class ClassStripVO extends ClassStripVO__Parameters {",
                        "    @Deprecated",
                        "    @Label(value = \"n\", max = 32)",
                        "    private String userName;",
                        "    @Deprecated",
                        "    private int countryCode;",
                        "}"));

        Compilation compilation = compile(label, source);
        assertThat(compilation).succeeded();
        com.google.common.truth.StringSubject generated = assertThat(compilation)
                .generatedSourceFile("com.example.ClassStripVO__Parameters")
                .contentsAsUtf8String();
        generated.contains("private String userName;");
        generated.contains("private int countryCode;");
        generated.doesNotContain("@Deprecated");
        generated.doesNotContain("@com.example.Label");
        generated.doesNotContain("max = 32");
    }

    @Test
    void ofRequiresClassOrType() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.EmptyOfVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import com.pojo.parameters.Parameters.Of;",
                        "",
                        "@Parameters(of = @Of(names = {\"value\"}))",
                        "public class EmptyOfVO extends EmptyOfVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Each @Of must declare Class or type");
    }

    @Test
    void mergesGenericFieldsConstantsAndAnnotations() {
        JavaFileObject label = JavaFileObjects.forSourceString(
                "com.example.Label",
                src(
                        "package com.example;",
                        "",
                        "import java.lang.annotation.ElementType;",
                        "import java.lang.annotation.Retention;",
                        "import java.lang.annotation.RetentionPolicy;",
                        "import java.lang.annotation.Target;",
                        "",
                        "@Target(ElementType.FIELD)",
                        "@Retention(RetentionPolicy.SOURCE)",
                        "public @interface Label {",
                        "    String value();",
                        "    int max() default 0;",
                        "}"));
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.FieldVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import java.util.List;",
                        "import java.util.Map;",
                        "",
                        "@Parameters",
                        "public class FieldVO extends FieldVO__Parameters {",
                        "    @Label(value = \"n\", max = 32)",
                        "    private String userName;",
                        "    private int countryCode = 86;",
                        "    private String status = \"ACTIVE\";",
                        "    private List<String> roles;",
                        "    private Map<String, Integer> counts;",
                        "}"));

        Compilation compilation = compile(label, source);
        assertThat(compilation).succeeded();
        com.google.common.truth.StringSubject generated = assertThat(compilation)
                .generatedSourceFile("com.example.FieldVO__Parameters")
                .contentsAsUtf8String();
        generated.contains("@com.example.Label(value = \"n\", max = 32)");
        generated.contains("private String userName;");
        generated.contains("private int countryCode = 86;");
        generated.contains("private String status = \"ACTIVE\";");
        generated.contains("private java.util.List<String> roles;");
        generated.contains("private java.util.Map<String,");
        generated.contains("counts;");
    }

    @Test
    void generatesSuperclassThatExtendsAndImplements() {
        JavaFileObject base = JavaFileObjects.forSourceString(
                "com.example.BaseEntity",
                src(
                        "package com.example;",
                        "",
                        "public abstract class BaseEntity {",
                        "    private Long version;",
                        "    public Long getVersion() { return version; }",
                        "    public void setVersion(Long version) { this.version = version; }",
                        "}"));
        JavaFileObject named = JavaFileObjects.forSourceString(
                "com.example.Named",
                src(
                        "package com.example;",
                        "",
                        "public interface Named {",
                        "    String getUserName();",
                        "    void setUserName(String userName);",
                        "}"));
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.StaffVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "import java.io.Serializable;",
                        "",
                        "@Parameters(",
                        "        String = {\"userName\"},",
                        "        Extends = BaseEntity.class,",
                        "        Implements = {Named.class, Serializable.class})",
                        "public class StaffVO extends StaffVO__Parameters {",
                        "}"));

        Compilation compilation = compile(base, named, source);
        assertThat(compilation).succeeded();
        com.google.common.truth.StringSubject generated = assertThat(compilation)
                .generatedSourceFile("com.example.StaffVO__Parameters")
                .contentsAsUtf8String();
        generated.contains("public abstract class StaffVO__Parameters extends com.example.BaseEntity implements com.example.Named, java.io.Serializable {");
        generated.contains("private String userName;");
        generated.contains("public String getUserName()");
        generated.doesNotContain("super.equals");
        generated.doesNotContain("super.hashCode");
        generated.doesNotContain("super.toString");
    }

    @Test
    void callsSuperWhenExtendedClassOverridesEqualsHashCodeAndToString() {
        JavaFileObject base = JavaFileObjects.forSourceString(
                "com.example.Counted",
                src(
                        "package com.example;",
                        "",
                        "public abstract class Counted {",
                        "    @Override public boolean equals(Object o) { return super.equals(o); }",
                        "    @Override public int hashCode() { return super.hashCode(); }",
                        "    @Override public String toString() { return \"Counted\"; }",
                        "}"));
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.CountedVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters(String = {\"label\"}, Extends = Counted.class)",
                        "public class CountedVO extends CountedVO__Parameters {",
                        "}"));

        Compilation compilation = compile(base, source);
        assertThat(compilation).succeeded();
        com.google.common.truth.StringSubject generated = assertThat(compilation)
                .generatedSourceFile("com.example.CountedVO__Parameters")
                .contentsAsUtf8String();
        generated.contains("extends com.example.Counted {");
        generated.contains("if (!super.equals(o))");
        generated.contains("return 31 * super.hashCode() + ");
        generated.contains("super=\" + super.toString()");
    }

    @Test
    void omitsExtendsClauseWhenObject() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.ObjVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters(String = {\"label\"}, Extends = Object.class)",
                        "public class ObjVO extends ObjVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.ObjVO__Parameters")
                .contentsAsUtf8String()
                .contains("public abstract class ObjVO__Parameters {");
    }

    @Test
    void rejectsExtendsOnInterface() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.IfaceVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters(String = {\"label\"}, Extends = Runnable.class)",
                        "public class IfaceVO extends IfaceVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Extends must be a class");
    }

    @Test
    void rejectsImplementsOnClass() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.ClassIfaceVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters(String = {\"label\"}, Implements = String.class)",
                        "public class ClassIfaceVO extends ClassIfaceVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Implements must be interfaces");
    }

    @Test
    void rejectsFinalExtends() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.FinalVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters(String = {\"label\"}, Extends = String.class)",
                        "public class FinalVO extends FinalVO__Parameters {",
                        "}"));

        Compilation compilation = compile(source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("cannot extend a final class");
    }

    @Test
    void rejectsExtendsWithoutNoArgConstructor() {
        JavaFileObject base = JavaFileObjects.forSourceString(
                "com.example.NeedsArg",
                src(
                        "package com.example;",
                        "",
                        "public class NeedsArg {",
                        "    public NeedsArg(String id) {",
                        "    }",
                        "}"));
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.NeedsArgVO",
                src(
                        "package com.example;",
                        "",
                        "import com.pojo.parameters.Parameters;",
                        "",
                        "@Parameters(String = {\"label\"}, Extends = NeedsArg.class)",
                        "public class NeedsArgVO extends NeedsArgVO__Parameters {",
                        "}"));

        Compilation compilation = compile(base, source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("accessible no-arg constructor");
    }

    private static Compilation compile(JavaFileObject... sources) {
        return javac()
                .withProcessors(new ParametersProcessor())
                .compile(sources);
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
