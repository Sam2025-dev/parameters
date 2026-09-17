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
                """
                        package com.example;

                        import com.pojo.parameters.Parameters;

                        @Parameters(String = {"userName"}, int_ = {1, 2, 3})
                        public class DemoVO extends DemoVO__Parameters {
                        }
                        """);

        Compilation compilation = compile(source);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.DemoVO__Parameters")
                .contentsAsUtf8String()
                .contains("private String userName;");
        assertThat(compilation)
                .generatedSourceFile("com.example.DemoVO__Parameters")
                .contentsAsUtf8String()
                .contains("private int int_1 = 1;");
        assertThat(compilation)
                .generatedSourceFile("com.example.DemoVO__Parameters")
                .contentsAsUtf8String()
                .contains("private int int_2 = 2;");
        assertThat(compilation)
                .generatedSourceFile("com.example.DemoVO__Parameters")
                .contentsAsUtf8String()
                .contains("private int int_3 = 3;");
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
                .contains("public int getInt_1()");
    }

    @Test
    void generatesAllPrimitiveShorthandsAndCustomTypes() {
        JavaFileObject address = JavaFileObjects.forSourceString(
                "com.example.Address",
                """
                        package com.example;

                        public class Address {
                            public String city;
                        }
                        """);
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.AllTypesVO",
                """
                        package com.example;

                        import com.pojo.parameters.Parameters;
                        import com.pojo.parameters.Parameters.Of;

                        @Parameters(
                                String = {"userName"},
                                boolean_ = {true},
                                byte_ = {1},
                                short_ = {2},
                                int_ = {3},
                                long_ = {4L},
                                char_ = {'Z'},
                                float_ = {1.5f},
                                double_ = {2.25},
                                of = {
                                        @Of(Class = Address.class),
                                        @Of(Class = Long.class, names = {"id"}),
                                        @Of(Class = String[].class, names = {"tags"}),
                                        @Of(Class = int.class, names = {"count"})
                                }
                        )
                        public class AllTypesVO extends AllTypesVO__Parameters {
                        }
                        """);

        Compilation compilation = compile(address, source);
        assertThat(compilation).succeeded();
        var generated = assertThat(compilation).generatedSourceFile("com.example.AllTypesVO__Parameters").contentsAsUtf8String();
        generated.contains("private String userName;");
        generated.contains("private boolean boolean_true = true;");
        generated.contains("private byte byte_1 = (byte) 1;");
        generated.contains("private short short_2 = (short) 2;");
        generated.contains("private int int_3 = 3;");
        generated.contains("private long long_4 = 4L;");
        generated.contains("private char char_Z = 'Z';");
        generated.contains("private float float_1_5 = 1.5f;");
        generated.contains("private double double_2_25 = 2.25;");
        generated.contains("private com.example.Address address;");
        generated.contains("private Long id;");
        generated.contains("private String[] tags;");
        generated.contains("private int count;");
        generated.contains("public boolean isBoolean_true()");
        generated.contains("public com.example.Address getAddress()");
    }

    @Test
    void mergesInstanceFieldsIntoGeneratedParameters() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.MergeVO",
                """
                        package com.example;

                        import com.pojo.parameters.Parameters;

                        @Parameters(String = {"userName"})
                        public class MergeVO extends MergeVO__Parameters {
                            private Long id;
                            private String label;
                        }
                        """);

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
                """
                        package com.example;

                        import com.pojo.parameters.Parameters;

                        @Parameters
                        public class OnlyFieldsVO extends OnlyFieldsVO__Parameters {
                            private String title;
                        }
                        """);

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
                """
                        package com.example;

                        import com.pojo.parameters.Parameters;

                        @Parameters
                        public class EmptyVO extends EmptyVO__Parameters {
                        }
                        """);

        Compilation compilation = compile(source);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("must declare at least one property or instance field to merge");
    }

    @Test
    void rejectsInvalidPropertyName() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.BadVO",
                """
                        package com.example;

                        import com.pojo.parameters.Parameters;

                        @Parameters(String = {"123oops"})
                        public class BadVO extends BadVO__Parameters {
                        }
                        """);

        Compilation compilation = compile(source);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Invalid String property name");
    }

    @Test
    void disambiguatesDuplicateIntValues() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.DupIntVO",
                """
                        package com.example;

                        import com.pojo.parameters.Parameters;

                        @Parameters(int_ = {1, 1})
                        public class DupIntVO extends DupIntVO__Parameters {
                        }
                        """);

        Compilation compilation = compile(source);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.DupIntVO__Parameters")
                .contentsAsUtf8String()
                .contains("private int int_1 = 1;");
        assertThat(compilation)
                .generatedSourceFile("com.example.DupIntVO__Parameters")
                .contentsAsUtf8String()
                .contains("private int int_1_2 = 1;");
    }

    @Test
    void mergesSameNameSameTypeInsteadOfFailing() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.DupVO",
                """
                        package com.example;

                        import com.pojo.parameters.Parameters;

                        @Parameters(String = {"userName"})
                        public class DupVO extends DupVO__Parameters {
                            private String userName;
                        }
                        """);

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
                """
                        package com.example;

                        import com.pojo.parameters.Parameters;
                        import com.pojo.parameters.Parameters.Of;

                        @Parameters(of = @Of(Class = Integer.class, names = {"id"}))
                        public class ConflictVO extends ConflictVO__Parameters {
                            private Long id;
                        }
                        """);

        Compilation compilation = compile(source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Duplicate property name with conflicting types: id");
    }

    @Test
    void ofDeclaresClassAndMultipleNames() {
        JavaFileObject address = JavaFileObjects.forSourceString(
                "com.example.Address",
                """
                        package com.example;

                        public class Address {
                            public String city;
                        }
                        """);
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.OfVO",
                """
                        package com.example;

                        import com.pojo.parameters.Parameters;
                        import com.pojo.parameters.Parameters.Of;

                        @Parameters(
                                int_ = {1},
                                long_ = {18L},
                                of = @Of(Class = Address.class, names = {"homeAddress", "workAddress"})
                        )
                        public class OfVO extends OfVO__Parameters {
                        }
                        """);

        Compilation compilation = compile(address, source);
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.OfVO__Parameters")
                .contentsAsUtf8String()
                .contains("private int int_1 = 1;");
        assertThat(compilation)
                .generatedSourceFile("com.example.OfVO__Parameters")
                .contentsAsUtf8String()
                .contains("private long long_18 = 18L;");
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
                """
                        package com.example;

                        public class Address {}
                        """);
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.DerivedNameVO",
                """
                        package com.example;

                        import com.pojo.parameters.Parameters;
                        import com.pojo.parameters.Parameters.Of;

                        @Parameters(of = @Of(Class = Address.class))
                        public class DerivedNameVO extends DerivedNameVO__Parameters {
                        }
                        """);

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
                """
                        package com.example;

                        import com.pojo.parameters.Parameters;
                        import com.pojo.parameters.Parameters.Of;

                        @Parameters(of = @Of(Class = Long.class))
                        public class BadOfVO extends BadOfVO__Parameters {
                        }
                        """);

        Compilation compilation = compile(source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("declare names explicitly on @Of");
    }

    @Test
    void rejectsInvalidOfPropertyName() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "com.example.BadNameVO",
                """
                        package com.example;

                        import com.pojo.parameters.Parameters;
                        import com.pojo.parameters.Parameters.Of;

                        @Parameters(of = @Of(Class = Long.class, names = {"123id"}))
                        public class BadNameVO extends BadNameVO__Parameters {
                        }
                        """);

        Compilation compilation = compile(source);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Invalid @Of property name");
    }

    private static Compilation compile(JavaFileObject... sources) {
        return javac()
                .withProcessors(new ParametersProcessor())
                .compile(sources);
    }
}
