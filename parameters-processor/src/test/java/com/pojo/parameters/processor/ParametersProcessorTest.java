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

        Compilation compilation = javac()
                .withProcessors(new ParametersProcessor())
                .compile(source);

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

        Compilation compilation = javac()
                .withProcessors(new ParametersProcessor())
                .compile(source);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("must declare at least one String or int_ property");
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

        Compilation compilation = javac()
                .withProcessors(new ParametersProcessor())
                .compile(source);

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

        Compilation compilation = javac()
                .withProcessors(new ParametersProcessor())
                .compile(source);

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
    void rejectsDuplicatePropertyName() {
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

        Compilation compilation = javac()
                .withProcessors(new ParametersProcessor())
                .compile(source);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Duplicate property name: userName");
    }
}
