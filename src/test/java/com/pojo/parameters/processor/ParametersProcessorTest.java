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
