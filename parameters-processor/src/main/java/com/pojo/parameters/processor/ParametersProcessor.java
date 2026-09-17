package com.pojo.parameters.processor;

import com.pojo.parameters.Parameters;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Assembles {@link Parameters} fields into a generated superclass at compile time.
 */
@SupportedAnnotationTypes("com.pojo.parameters.Parameters")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class ParametersProcessor extends AbstractProcessor {

    static final String SUPERCLASS_SUFFIX = "__Parameters";

    private final Set<String> written = new LinkedHashSet<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(Parameters.class)) {
            processElement(element);
        }
        return false;
    }

    private void processElement(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            error(element, "@Parameters can only be placed on classes.");
            return;
        }
        TypeElement type = (TypeElement) element;
        if (type.getNestingKind() != NestingKind.TOP_LEVEL) {
            error(element, "@Parameters can only be placed on top-level classes.");
            return;
        }
        if (type.getModifiers().contains(Modifier.FINAL)) {
            error(element, "@Parameters cannot be placed on final classes; they must extend the generated superclass.");
            return;
        }
        Parameters annotation = type.getAnnotation(Parameters.class);
        if (annotation == null) {
            return;
        }
        AssembledProperties properties = AssembledProperties.from(type, annotation, this::error);
        if (properties == null) {
            return;
        }
        String superName = type.getSimpleName() + SUPERCLASS_SUFFIX;
        String packageName = packageName(type);
        String qualified = packageName.isEmpty() ? superName : packageName + '.' + superName;
        if (!written.add(qualified)) {
            return;
        }
        try {
            writeSupportClass(type, packageName, superName, qualified, properties);
        } catch (IOException ex) {
            error(type, "Failed to write " + qualified + ": " + ex.getMessage());
        }
    }

    private void writeSupportClass(
            TypeElement originating,
            String packageName,
            String superName,
            String qualified,
            AssembledProperties properties) throws IOException {
        Filer filer = processingEnv.getFiler();
        JavaFileObject file = filer.createSourceFile(qualified, originating);
        try (Writer writer = file.openWriter()) {
            writer.write(properties.renderJava(packageName, superName));
        }
    }

    static String packageName(TypeElement type) {
        String qualified = type.getQualifiedName().toString();
        int lastDot = qualified.lastIndexOf('.');
        return lastDot < 0 ? "" : qualified.substring(0, lastDot);
    }

    void error(Element element, String message) {
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    record StringProperty(String name) {
        String getter() {
            return accessor("get", name);
        }

        String setter() {
            return accessor("set", name);
        }
    }

    record IntProperty(String name, int value) {
        String getter() {
            return accessor("get", name);
        }

        String setter() {
            return accessor("set", name);
        }
    }

    static String accessor(String prefix, String fieldName) {
        return prefix + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    static final class AssembledProperties {
        private final List<StringProperty> strings;
        private final List<IntProperty> ints;

        AssembledProperties(List<StringProperty> strings, List<IntProperty> ints) {
            this.strings = strings;
            this.ints = ints;
        }

        static AssembledProperties from(TypeElement type, Parameters annotation, java.util.function.BiConsumer<Element, String> error) {
            Set<String> used = existingFieldNames(type);
            List<StringProperty> strings = new ArrayList<>();
            for (String name : annotation.String()) {
                if (!SourceVersion.isIdentifier(name) || SourceVersion.isKeyword(name)) {
                    error.accept(type, "Invalid String property name: " + name);
                    return null;
                }
                if (!used.add(name)) {
                    error.accept(type, "Duplicate property name: " + name);
                    return null;
                }
                strings.add(new StringProperty(name));
            }
            List<IntProperty> ints = new ArrayList<>();
            for (int value : annotation.int_()) {
                ints.add(new IntProperty(uniqueIntName(value, used), value));
            }
            if (strings.isEmpty() && ints.isEmpty()) {
                error.accept(type, "@Parameters must declare at least one String or int_ property.");
                return null;
            }
            return new AssembledProperties(List.copyOf(strings), List.copyOf(ints));
        }

        private static Set<String> existingFieldNames(TypeElement type) {
            Set<String> names = new LinkedHashSet<>();
            for (Element enclosed : type.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.FIELD && enclosed instanceof VariableElement field) {
                    names.add(field.getSimpleName().toString());
                }
            }
            return names;
        }

        private static String uniqueIntName(int value, Set<String> used) {
            String base = "int_" + value;
            if (used.add(base)) {
                return base;
            }
            int suffix = 2;
            String candidate;
            do {
                candidate = base + '_' + suffix;
                suffix++;
            } while (used.contains(candidate));
            used.add(candidate);
            return candidate;
        }

        String renderJava(String packageName, String className) {
            StringBuilder java = new StringBuilder();
            if (!packageName.isEmpty()) {
                java.append("package ").append(packageName).append(";\n\n");
            }
            java.append("import javax.annotation.processing.Generated;\n\n");
            java.append("/**\n");
            java.append(" * Generated by {@code @Parameters}. Do not edit.\n");
            java.append(" */\n");
            java.append("@Generated(\"com.pojo.parameters.processor.ParametersProcessor\")\n");
            java.append("public abstract class ").append(className).append(" {\n\n");
            for (StringProperty property : strings) {
                java.append("    private String ").append(property.name()).append(";\n");
            }
            for (IntProperty property : ints) {
                java.append("    private int ").append(property.name())
                        .append(" = ").append(property.value()).append(";\n");
            }
            java.append('\n');
            java.append("    protected ").append(className).append("() {\n");
            java.append("    }\n");
            for (StringProperty property : strings) {
                appendStringAccessors(java, property);
            }
            for (IntProperty property : ints) {
                appendIntAccessors(java, property);
            }
            java.append("}\n");
            return java.toString();
        }

        private static void appendStringAccessors(StringBuilder java, StringProperty property) {
            java.append('\n');
            java.append("    public String ").append(property.getter()).append("() {\n");
            java.append("        return this.").append(property.name()).append(";\n");
            java.append("    }\n\n");
            java.append("    public void ").append(property.setter()).append("(String ").append(property.name()).append(") {\n");
            java.append("        this.").append(property.name()).append(" = ").append(property.name()).append(";\n");
            java.append("    }\n");
        }

        private static void appendIntAccessors(StringBuilder java, IntProperty property) {
            java.append('\n');
            java.append("    public int ").append(property.getter()).append("() {\n");
            java.append("        return this.").append(property.name()).append(";\n");
            java.append("    }\n\n");
            java.append("    public void ").append(property.setter()).append("(int ").append(property.name()).append(") {\n");
            java.append("        this.").append(property.name()).append(" = ").append(property.name()).append(";\n");
            java.append("    }\n");
        }
    }
}
