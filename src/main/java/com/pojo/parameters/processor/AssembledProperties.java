package com.pojo.parameters.processor;

import com.pojo.parameters.Parameters;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Collects annotation-declared properties and instance fields into one list.
 */
final class AssembledProperties {

    private static final List<String> INTEGRAL_PRIMITIVES =
            Arrays.asList("byte", "short", "int", "long", "char");

    private final List<AssembledProperty> properties;
    private final String superclass;
    private final List<String> interfaces;
    private final boolean callSuperEquals;
    private final boolean callSuperToString;

    AssembledProperties(List<AssembledProperty> properties) {
        this(properties, null, Collections.<String>emptyList(), false, false);
    }

    AssembledProperties(
            List<AssembledProperty> properties,
            String superclass,
            List<String> interfaces,
            boolean callSuperEquals,
            boolean callSuperToString) {
        this.properties = Collections.unmodifiableList(new ArrayList<AssembledProperty>(properties));
        this.superclass = superclass;
        this.interfaces = Collections.unmodifiableList(new ArrayList<String>(
                interfaces == null ? Collections.<String>emptyList() : interfaces));
        this.callSuperEquals = callSuperEquals;
        this.callSuperToString = callSuperToString;
    }

    List<AssembledProperty> properties() {
        return properties;
    }

    boolean isEmpty() {
        return properties.isEmpty();
    }

    static AssembledProperties from(
            TypeElement type,
            Parameters annotation,
            AnnotationMirror mirror,
            Elements elements,
            Function<VariableElement, String> fieldInitializer,
            BiConsumer<Element, String> error) {
        Map<String, AssembledProperty> byName = new LinkedHashMap<String, AssembledProperty>();

        if (!mergeExistingFields(type, fieldInitializer, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.String(), "String", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.Boolean(), "Boolean", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.Byte(), "Byte", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.Short(), "Short", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.Integer(), "Integer", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.Long(), "Long", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.Character(), "Character", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.Float(), "Float", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.Double(), "Double", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.boolean_(), "boolean", true, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.byte_(), "byte", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.short_(), "short", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.int_(), "int", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.long_(), "long", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.char_(), "char", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.float_(), "float", false, byName, error)) {
            return null;
        }
        if (!addNamedProperties(type, annotation.double_(), "double", false, byName, error)) {
            return null;
        }
        if (!addOfProperties(type, mirror, elements, byName, error)) {
            return null;
        }
        if (byName.isEmpty()) {
            error.accept(type, "@Parameters must declare at least one property or instance field to merge.");
            return null;
        }
        if (!applyParametersRemoveAnnot(type, mirror, elements, byName, error)) {
            return null;
        }
        Heritage heritage = resolveHeritage(type, mirror, elements, error);
        if (heritage == null) {
            return null;
        }
        return new AssembledProperties(
                new ArrayList<AssembledProperty>(byName.values()),
                heritage.superclass,
                heritage.interfaces,
                heritage.callSuperEquals,
                heritage.callSuperToString);
    }

    String renderJava(String packageName, String className) {
        StringBuilder java = new StringBuilder();
        if (!packageName.isEmpty()) {
            java.append("package ").append(packageName).append(";\n\n");
        }
        if (hasArrayProperty()) {
            java.append("import java.util.Arrays;\n");
        }
        java.append("import java.util.Objects;\n\n");
        java.append("/**\n");
        java.append(" * Generated by {@code @Parameters}. Do not edit.\n");
        java.append(" */\n");
        java.append("public abstract class ").append(className);
        if (superclass != null) {
            java.append(" extends ").append(superclass);
        }
        if (!interfaces.isEmpty()) {
            java.append(" implements ");
            for (int i = 0; i < interfaces.size(); i++) {
                if (i > 0) {
                    java.append(", ");
                }
                java.append(interfaces.get(i));
            }
        }
        java.append(" {\n\n");
        for (AssembledProperty property : properties) {
            for (String annotation : property.annotations()) {
                java.append("    ").append(annotation).append('\n');
            }
            java.append("    private ").append(property.typeSource()).append(' ').append(property.name());
            if (property.initializer() != null) {
                java.append(" = ").append(property.initializer());
            }
            java.append(";\n");
        }
        java.append('\n');
        java.append("    protected ").append(className).append("() {\n");
        java.append("    }\n");
        for (AssembledProperty property : properties) {
            appendAccessors(java, property);
        }
        appendEqualsHashCodeToString(java, className);
        java.append("}\n");
        return java.toString();
    }

    private boolean hasArrayProperty() {
        for (AssembledProperty property : properties) {
            if (property.array()) {
                return true;
            }
        }
        return false;
    }

    private static boolean mergeExistingFields(
            TypeElement type,
            Function<VariableElement, String> fieldInitializer,
            Map<String, AssembledProperty> byName,
            BiConsumer<Element, String> error) {
        for (Element enclosed : type.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.FIELD || !(enclosed instanceof VariableElement)) {
                continue;
            }
            VariableElement field = (VariableElement) enclosed;
            if (field.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            String name = field.getSimpleName().toString();
            if (!SourceVersion.isIdentifier(name) || SourceVersion.isKeyword(name)) {
                error.accept(field, "Cannot merge field into @Parameters: invalid name " + name);
                return false;
            }
            AssembledProperty property = AssembledProperty.fromType(
                    name,
                    field.asType(),
                    Collections.<TypeMirror>emptyList(),
                    fieldInitializer.apply(field),
                    renderFieldAnnotations(field));
            if (!putMerged(type, byName, property, error)) {
                return false;
            }
        }
        return true;
    }

    private static boolean addOfProperties(
            TypeElement type,
            AnnotationMirror mirror,
            Elements elements,
            Map<String, AssembledProperty> byName,
            BiConsumer<Element, String> error) {
        if (mirror == null) {
            return true;
        }
        for (AnnotationMirror nested : nestedAnnotations(mirror, elements, "of")) {
            TypeMirror propertyType = classValue(nested, elements, "Class");
            boolean classSpecified = propertyType != null && propertyType.getKind() != TypeKind.VOID;
            String typeOverride = optionalString(nested, elements, "type");
            if (!classSpecified && typeOverride == null) {
                error.accept(type, "Each @Of must declare Class or type.");
                return false;
            }
            List<TypeMirror> typeArgs = classValues(nested, elements, "typeArgs");
            if (typeOverride != null && !typeArgs.isEmpty()) {
                error.accept(type, "@Of type and typeArgs cannot be combined; put nested generics in type.");
                return false;
            }
            if (!typeArgs.isEmpty()) {
                if (!classSpecified) {
                    error.accept(type, "@Of typeArgs requires Class.");
                    return false;
                }
                int expected = typeParameterCount(propertyType);
                if (expected == 0) {
                    error.accept(type, "typeArgs can only be used with generic types: " + propertyType);
                    return false;
                }
                if (typeArgs.size() != expected) {
                    error.accept(type, "Type " + propertyType + " expects " + expected
                            + " type argument(s), not " + typeArgs.size() + ".");
                    return false;
                }
                for (TypeMirror arg : typeArgs) {
                    if (arg.getKind() == TypeKind.VOID) {
                        error.accept(type, "Invalid typeArgs value: void.");
                        return false;
                    }
                }
            }
            String initializer = optionalString(nested, elements, "initializer");
            List<String> names = stringValues(nested, elements, "names");
            if (names.isEmpty()) {
                String derived = typeOverride != null
                        ? decapitalize(simpleNameFromSource(typeOverride))
                        : decapitalize(simpleName(propertyType));
                if (!SourceVersion.isIdentifier(derived) || SourceVersion.isKeyword(derived)) {
                    error.accept(type, "Type " + (typeOverride != null ? typeOverride : propertyType)
                            + " produces invalid field name '" + derived
                            + "'; declare names explicitly on @Of.");
                    return false;
                }
                names = Collections.singletonList(derived);
            }
            for (String name : names) {
                if (!SourceVersion.isIdentifier(name) || SourceVersion.isKeyword(name)) {
                    error.accept(type, "Invalid @Of property name: " + name);
                    return false;
                }
                AssembledProperty property = typeOverride != null
                        ? AssembledProperty.fromSource(
                                name, typeOverride, initializer, Collections.<String>emptyList())
                        : AssembledProperty.fromType(
                                name, propertyType, typeArgs, initializer, Collections.<String>emptyList());
                if (!putMerged(type, byName, property, error)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void applyRemovedAnnotations(
            Map<String, AssembledProperty> byName,
            String name,
            List<String> removedAnnotations) {
        if (removedAnnotations.isEmpty()) {
            return;
        }
        AssembledProperty property = byName.get(name);
        if (property != null) {
            byName.put(name, property.withoutAnnotationTypes(removedAnnotations));
        }
    }

    private static boolean applyParametersRemoveAnnot(
            TypeElement type,
            AnnotationMirror mirror,
            Elements elements,
            Map<String, AssembledProperty> byName,
            BiConsumer<Element, String> error) {
        if (mirror == null) {
            return true;
        }
        List<String> removedAnnotations = new ArrayList<String>();
        if (!collectRemovedAnnotations(
                type, mirror, elements, "@Parameters removeAnnot", removedAnnotations, error)) {
            return false;
        }
        if (removedAnnotations.isEmpty()) {
            return true;
        }
        for (String name : new ArrayList<String>(byName.keySet())) {
            applyRemovedAnnotations(byName, name, removedAnnotations);
        }
        return true;
    }

    private static Heritage resolveHeritage(
            TypeElement type,
            AnnotationMirror mirror,
            Elements elements,
            BiConsumer<Element, String> error) {
        if (mirror == null) {
            return Heritage.NONE;
        }
        TypeMirror extendMirror = classValue(mirror, elements, "Extends");
        String superclass = null;
        TypeElement superType = null;
        if (extendMirror != null && extendMirror.getKind() != TypeKind.VOID) {
            superType = asTypeElement(extendMirror);
            if (superType == null || superType.getKind() != ElementKind.CLASS) {
                error.accept(type, "@Parameters Extends must be a class: " + extendMirror);
                return null;
            }
            if (isJavaLangObject(superType)) {
                superType = null;
            } else if (!validateSuperclass(type, superType, error)) {
                return null;
            } else {
                superclass = AssembledProperty.renderType(extendMirror);
            }
        }
        LinkedHashSet<String> interfaces = new LinkedHashSet<String>();
        for (TypeMirror ifaceMirror : classValues(mirror, elements, "Implements")) {
            TypeElement iface = asTypeElement(ifaceMirror);
            if (iface == null || iface.getKind() != ElementKind.INTERFACE) {
                error.accept(type, "@Parameters Implements must be interfaces: " + ifaceMirror);
                return null;
            }
            if (!isAccessibleFrom(type, iface)) {
                error.accept(type, "@Parameters Implements type must be accessible: " + ifaceMirror);
                return null;
            }
            interfaces.add(AssembledProperty.renderType(ifaceMirror));
        }
        boolean callSuperEquals = superType != null
                && (hierarchyDeclares(superType, "equals", 1) || hierarchyDeclares(superType, "hashCode", 0));
        boolean callSuperToString = superType != null && hierarchyDeclares(superType, "toString", 0);
        return new Heritage(
                superclass,
                new ArrayList<String>(interfaces),
                callSuperEquals,
                callSuperToString);
    }

    private static boolean validateSuperclass(
            TypeElement type,
            TypeElement superType,
            BiConsumer<Element, String> error) {
        if (type.getQualifiedName().contentEquals(superType.getQualifiedName())) {
            error.accept(type, "@Parameters Extends cannot be the annotated class itself.");
            return false;
        }
        if (superType.getModifiers().contains(Modifier.FINAL)) {
            error.accept(type, "@Parameters cannot extend a final class: " + superType.getQualifiedName());
            return false;
        }
        NestingKind nesting = superType.getNestingKind();
        if (nesting == NestingKind.INNER || nesting == NestingKind.ANONYMOUS || nesting == NestingKind.LOCAL) {
            error.accept(type, "@Parameters Extends cannot be an inner class: " + superType.getQualifiedName());
            return false;
        }
        if (!isAccessibleFrom(type, superType)) {
            error.accept(type, "@Parameters Extends type must be accessible: " + superType.getQualifiedName());
            return false;
        }
        if (!hasAccessibleNoArgConstructor(type, superType)) {
            error.accept(type, "@Parameters Extends type must have an accessible no-arg constructor: "
                    + superType.getQualifiedName());
            return false;
        }
        return true;
    }

    private static boolean isAccessibleFrom(TypeElement annotated, TypeElement target) {
        if (target.getModifiers().contains(Modifier.PUBLIC)) {
            return true;
        }
        return ParametersProcessor.packageName(annotated).equals(ParametersProcessor.packageName(target));
    }

    private static boolean hasAccessibleNoArgConstructor(TypeElement annotated, TypeElement superType) {
        boolean anyConstructor = false;
        for (Element enclosed : superType.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.CONSTRUCTOR || !(enclosed instanceof ExecutableElement)) {
                continue;
            }
            anyConstructor = true;
            ExecutableElement constructor = (ExecutableElement) enclosed;
            if (!constructor.getParameters().isEmpty()) {
                continue;
            }
            Set<Modifier> modifiers = constructor.getModifiers();
            if (modifiers.contains(Modifier.PRIVATE)) {
                continue;
            }
            if (modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PROTECTED)) {
                return true;
            }
            if (ParametersProcessor.packageName(annotated).equals(ParametersProcessor.packageName(superType))) {
                return true;
            }
        }
        return !anyConstructor;
    }

    private static boolean hierarchyDeclares(TypeElement type, String methodName, int parameterCount) {
        TypeElement current = type;
        while (current != null && !isJavaLangObject(current)) {
            for (Element enclosed : current.getEnclosedElements()) {
                if (enclosed.getKind() != ElementKind.METHOD || !(enclosed instanceof ExecutableElement)) {
                    continue;
                }
                ExecutableElement method = (ExecutableElement) enclosed;
                if (!method.getSimpleName().contentEquals(methodName)
                        || method.getParameters().size() != parameterCount) {
                    continue;
                }
                if ("equals".equals(methodName)) {
                    TypeMirror parameter = method.getParameters().get(0).asType();
                    if (isJavaLangObject(parameter)) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            current = asTypeElement(current.getSuperclass());
        }
        return false;
    }

    private static TypeElement asTypeElement(TypeMirror type) {
        if (type == null || type.getKind() != TypeKind.DECLARED) {
            return null;
        }
        Element element = ((DeclaredType) type).asElement();
        return element instanceof TypeElement ? (TypeElement) element : null;
    }

    private static boolean isJavaLangObject(TypeElement type) {
        return "java.lang.Object".contentEquals(type.getQualifiedName());
    }

    private static boolean isJavaLangObject(TypeMirror type) {
        TypeElement element = asTypeElement(type);
        return element != null && isJavaLangObject(element);
    }

    private static final class Heritage {
        static final Heritage NONE = new Heritage(null, Collections.<String>emptyList(), false, false);

        final String superclass;
        final List<String> interfaces;
        final boolean callSuperEquals;
        final boolean callSuperToString;

        Heritage(
                String superclass,
                List<String> interfaces,
                boolean callSuperEquals,
                boolean callSuperToString) {
            this.superclass = superclass;
            this.interfaces = interfaces;
            this.callSuperEquals = callSuperEquals;
            this.callSuperToString = callSuperToString;
        }
    }

    private static boolean collectRemovedAnnotations(
            TypeElement type,
            AnnotationMirror nested,
            Elements elements,
            String errorLabel,
            List<String> removedAnnotations,
            BiConsumer<Element, String> error) {
        for (TypeMirror annotationType : classValues(nested, elements, "removeAnnot")) {
            if (annotationType.getKind() != TypeKind.DECLARED) {
                error.accept(type, errorLabel + " must be annotation types: " + annotationType);
                return false;
            }
            Element annotationElement = ((DeclaredType) annotationType).asElement();
            if (annotationElement.getKind() != ElementKind.ANNOTATION_TYPE) {
                error.accept(type, errorLabel + " must be annotation types: " + annotationType);
                return false;
            }
            String rendered = AssembledProperty.renderType(annotationType);
            if (!removedAnnotations.contains(rendered)) {
                removedAnnotations.add(rendered);
            }
        }
        return true;
    }

    private static boolean addNamedProperties(
            TypeElement type,
            String[] names,
            String typeSource,
            boolean primitiveBoolean,
            Map<String, AssembledProperty> byName,
            BiConsumer<Element, String> error) {
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (!SourceVersion.isIdentifier(name) || SourceVersion.isKeyword(name)) {
                error.accept(type, "Invalid " + typeSource + " property name: " + name);
                return false;
            }
            AssembledProperty property = new AssembledProperty(
                    name, typeSource, null, primitiveBoolean, false, Collections.<String>emptyList());
            if (!putMerged(type, byName, property, error)) {
                return false;
            }
        }
        return true;
    }

    private static boolean putMerged(
            TypeElement type,
            Map<String, AssembledProperty> byName,
            AssembledProperty property,
            BiConsumer<Element, String> error) {
        AssembledProperty existing = byName.get(property.name());
        if (existing == null) {
            byName.put(property.name(), property);
            return true;
        }
        if (existing.typeSource().equals(property.typeSource())) {
            byName.put(property.name(), existing.mergeWith(property));
            return true;
        }
        error.accept(type, "Duplicate property name with conflicting types: " + property.name()
                + " (" + existing.typeSource() + " vs " + property.typeSource() + ")");
        return false;
    }

    private static void appendAccessors(StringBuilder java, AssembledProperty property) {
        java.append('\n');
        java.append("    public ").append(property.typeSource()).append(' ')
                .append(property.getter()).append("() {\n");
        java.append("        return this.").append(property.name()).append(";\n");
        java.append("    }\n\n");
        java.append("    public void ").append(property.setter()).append('(')
                .append(property.typeSource()).append(' ').append(property.name()).append(") {\n");
        java.append("        this.").append(property.name()).append(" = ").append(property.name()).append(";\n");
        java.append("    }\n");
    }

    private void appendEqualsHashCodeToString(StringBuilder java, String className) {
        java.append('\n');
        java.append("    @Override\n");
        java.append("    public boolean equals(Object o) {\n");
        java.append("        if (this == o) {\n");
        java.append("            return true;\n");
        java.append("        }\n");
        java.append("        if (!(o instanceof ").append(className).append(")) {\n");
        java.append("            return false;\n");
        java.append("        }\n");
        if (callSuperEquals) {
            java.append("        if (!super.equals(o)) {\n");
            java.append("            return false;\n");
            java.append("        }\n");
        }
        java.append("        ").append(className).append(" that = (").append(className).append(") o;\n");
        if (properties.isEmpty()) {
            java.append("        return true;\n");
        } else {
            java.append("        return ").append(equalsExpression()).append(";\n");
        }
        java.append("    }\n\n");
        java.append("    @Override\n");
        java.append("    public int hashCode() {\n");
        if (callSuperEquals) {
            java.append("        return 31 * super.hashCode() + ").append(hashExpression()).append(";\n");
        } else {
            java.append("        return ").append(hashExpression()).append(";\n");
        }
        java.append("    }\n\n");
        java.append("    @Override\n");
        java.append("    public String toString() {\n");
        java.append("        return \"").append(className).append("{\"\n");
        for (int i = 0; i < properties.size(); i++) {
            AssembledProperty property = properties.get(i);
            java.append("                + \"");
            if (i > 0) {
                java.append(", ");
            }
            java.append(property.name()).append("=\" + ");
            java.append(toStringFragment(property)).append('\n');
        }
        if (callSuperToString) {
            java.append("                + \"");
            if (!properties.isEmpty()) {
                java.append(", ");
            }
            java.append("super=\" + super.toString()\n");
        }
        java.append("                + '}';\n");
        java.append("    }\n");
    }

    private String equalsExpression() {
        List<String> parts = new ArrayList<String>();
        for (AssembledProperty property : properties) {
            String name = property.name();
            if (property.array()) {
                parts.add("Arrays.equals(this." + name + ", that." + name + ")");
            } else if ("float".equals(property.typeSource())) {
                parts.add("Float.compare(this." + name + ", that." + name + ") == 0");
            } else if ("double".equals(property.typeSource())) {
                parts.add("Double.compare(this." + name + ", that." + name + ") == 0");
            } else if (property.primitiveBoolean() || INTEGRAL_PRIMITIVES.contains(property.typeSource())) {
                parts.add("this." + name + " == that." + name);
            } else {
                parts.add("Objects.equals(this." + name + ", that." + name + ")");
            }
        }
        StringBuilder joined = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                joined.append("\n                && ");
            }
            joined.append(parts.get(i));
        }
        return joined.toString();
    }

    private String hashExpression() {
        if (properties.isEmpty()) {
            return "0";
        }
        boolean anyArray = hasArrayProperty();
        if (!anyArray) {
            StringBuilder args = new StringBuilder();
            for (int i = 0; i < properties.size(); i++) {
                if (i > 0) {
                    args.append(", ");
                }
                args.append("this.").append(properties.get(i).name());
            }
            return "Objects.hash(" + args + ")";
        }
        StringBuilder expr = new StringBuilder("1");
        for (AssembledProperty property : properties) {
            expr.append(" * 31 + ");
            if (property.array()) {
                expr.append("Arrays.hashCode(this.").append(property.name()).append(')');
            } else {
                expr.append("Objects.hashCode(this.").append(property.name()).append(')');
            }
        }
        return expr.toString();
    }

    private static String toStringFragment(AssembledProperty property) {
        if (property.array()) {
            return "Arrays.toString(this." + property.name() + ")";
        }
        return "this." + property.name();
    }

    static String decapitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    static String simpleName(TypeMirror type) {
        return simpleNameFromSource(type.toString());
    }

    static String simpleNameFromSource(String type) {
        String core = stripTopLevelGenerics(type.trim());
        while (core.endsWith("[]")) {
            core = core.substring(0, core.length() - 2).trim();
        }
        int lastDot = core.lastIndexOf('.');
        return lastDot < 0 ? core : core.substring(lastDot + 1);
    }

    static String stripTopLevelGenerics(String type) {
        int depth = 0;
        StringBuilder stripped = new StringBuilder();
        for (int i = 0; i < type.length(); i++) {
            char c = type.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
            } else if (depth == 0) {
                stripped.append(c);
            }
        }
        return stripped.toString().trim();
    }

    static List<AnnotationMirror> nestedAnnotations(AnnotationMirror mirror, Elements elements, String member) {
        Object value = annotationValue(mirror, elements, member);
        if (!(value instanceof List<?>)) {
            return Collections.emptyList();
        }
        List<?> list = (List<?>) value;
        List<AnnotationMirror> nested = new ArrayList<AnnotationMirror>();
        for (Object item : list) {
            Object raw = item instanceof AnnotationValue ? ((AnnotationValue) item).getValue() : item;
            if (raw instanceof AnnotationMirror) {
                nested.add((AnnotationMirror) raw);
            }
        }
        return nested;
    }

    static TypeMirror classValue(AnnotationMirror mirror, Elements elements, String member) {
        Object value = annotationValue(mirror, elements, member);
        return value instanceof TypeMirror ? (TypeMirror) value : null;
    }

    static List<TypeMirror> classValues(AnnotationMirror mirror, Elements elements, String member) {
        Object value = annotationValue(mirror, elements, member);
        if (value instanceof TypeMirror) {
            return Collections.singletonList((TypeMirror) value);
        }
        if (!(value instanceof List<?>)) {
            return Collections.emptyList();
        }
        List<?> list = (List<?>) value;
        List<TypeMirror> types = new ArrayList<TypeMirror>();
        for (Object item : list) {
            Object raw = item instanceof AnnotationValue ? ((AnnotationValue) item).getValue() : item;
            if (raw instanceof TypeMirror) {
                types.add((TypeMirror) raw);
            }
        }
        return types;
    }

    static List<String> stringValues(AnnotationMirror mirror, Elements elements, String member) {
        Object value = annotationValue(mirror, elements, member);
        if (value instanceof String) {
            return Collections.singletonList((String) value);
        }
        if (!(value instanceof List<?>)) {
            return Collections.emptyList();
        }
        List<?> list = (List<?>) value;
        List<String> names = new ArrayList<String>();
        for (Object item : list) {
            Object raw = item instanceof AnnotationValue ? ((AnnotationValue) item).getValue() : item;
            if (raw instanceof String) {
                names.add((String) raw);
            }
        }
        return names;
    }

    static String optionalString(AnnotationMirror mirror, Elements elements, String member) {
        Object value = annotationValue(mirror, elements, member);
        if (!(value instanceof String)) {
            return null;
        }
        String text = ((String) value).trim();
        return text.isEmpty() ? null : text;
    }

    static Object annotationValue(AnnotationMirror mirror, Elements elements, String member) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                : elements.getElementValuesWithDefaults(mirror).entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(member)) {
                return entry.getValue().getValue();
            }
        }
        return null;
    }

    static int typeParameterCount(TypeMirror propertyType) {
        if (propertyType.getKind() != TypeKind.DECLARED) {
            return 0;
        }
        Element element = ((DeclaredType) propertyType).asElement();
        if (!(element instanceof TypeElement)) {
            return 0;
        }
        return ((TypeElement) element).getTypeParameters().size();
    }

    static List<String> renderFieldAnnotations(VariableElement field) {
        List<String> annotations = new ArrayList<String>();
        for (AnnotationMirror mirror : field.getAnnotationMirrors()) {
            if (!shouldCopyFieldAnnotation(mirror)) {
                continue;
            }
            annotations.add(renderAnnotationMirror(mirror));
        }
        return annotations;
    }

    static boolean shouldCopyFieldAnnotation(AnnotationMirror mirror) {
        Element annotationElement = mirror.getAnnotationType().asElement();
        if (annotationElement.getKind() != ElementKind.ANNOTATION_TYPE) {
            return false;
        }
        TypeElement annotationType = (TypeElement) annotationElement;
        String qualified = annotationType.getQualifiedName().toString();
        if (qualified.startsWith("lombok.") || qualified.startsWith("com.pojo.parameters.")) {
            return false;
        }
        Target target = annotationType.getAnnotation(Target.class);
        if (target == null) {
            return true;
        }
        ElementType[] targets = target.value();
        for (int i = 0; i < targets.length; i++) {
            if (targets[i] == ElementType.FIELD || targets[i] == ElementType.TYPE_USE) {
                return true;
            }
        }
        return false;
    }

    static String renderAnnotationMirror(AnnotationMirror mirror) {
        StringBuilder rendered = new StringBuilder();
        rendered.append('@').append(AssembledProperty.renderType(mirror.getAnnotationType()));
        Map<? extends ExecutableElement, ? extends AnnotationValue> explicit = mirror.getElementValues();
        if (explicit.isEmpty()) {
            return rendered.toString();
        }
        rendered.append('(');
        if (explicit.size() == 1) {
            Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> only =
                    explicit.entrySet().iterator().next();
            if (only.getKey().getSimpleName().contentEquals("value")) {
                rendered.append(renderAnnotationValue(only.getValue()));
                rendered.append(')');
                return rendered.toString();
            }
        }
        boolean first = true;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : explicit.entrySet()) {
            if (!first) {
                rendered.append(", ");
            }
            first = false;
            rendered.append(entry.getKey().getSimpleName()).append(" = ")
                    .append(renderAnnotationValue(entry.getValue()));
        }
        rendered.append(')');
        return rendered.toString();
    }

    static String renderAnnotationValue(AnnotationValue value) {
        Object raw = value.getValue();
        if (raw instanceof String) {
            return '"' + escapeJava((String) raw) + '"';
        }
        if (raw instanceof Character) {
            return "'" + escapeJava(String.valueOf((Character) raw)) + "'";
        }
        if (raw instanceof TypeMirror) {
            return AssembledProperty.renderType((TypeMirror) raw) + ".class";
        }
        if (raw instanceof VariableElement) {
            VariableElement enumConst = (VariableElement) raw;
            return enumConst.getEnclosingElement() + "." + enumConst.getSimpleName();
        }
        if (raw instanceof AnnotationMirror) {
            return renderAnnotationMirror((AnnotationMirror) raw);
        }
        if (raw instanceof List<?>) {
            List<?> items = (List<?>) raw;
            if (items.isEmpty()) {
                return "{}";
            }
            StringBuilder rendered = new StringBuilder("{");
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) {
                    rendered.append(", ");
                }
                Object item = items.get(i);
                if (item instanceof AnnotationValue) {
                    rendered.append(renderAnnotationValue((AnnotationValue) item));
                } else {
                    rendered.append(item);
                }
            }
            rendered.append('}');
            return rendered.toString();
        }
        if (raw instanceof Long) {
            return raw.toString() + "L";
        }
        if (raw instanceof Float) {
            return raw.toString() + "F";
        }
        if (raw instanceof Double) {
            return raw.toString() + "D";
        }
        return String.valueOf(raw);
    }

    static String renderConstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return '"' + escapeJava((String) value) + '"';
        }
        if (value instanceof Character) {
            return "'" + escapeJava(String.valueOf((Character) value)) + "'";
        }
        if (value instanceof Long) {
            return value.toString() + "L";
        }
        if (value instanceof Float) {
            return value.toString() + "F";
        }
        if (value instanceof Double) {
            return value.toString() + "D";
        }
        return String.valueOf(value);
    }

    static String escapeJava(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\'':
                    escaped.append("\\'");
                    break;
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < 32 || c == 127) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                    break;
            }
        }
        return escaped.toString();
    }

    static final class AssembledProperty {

        private final String name;
        private final String typeSource;
        private final String initializer;
        private final boolean primitiveBoolean;
        private final boolean array;
        private final List<String> annotations;

        AssembledProperty(
                String name,
                String typeSource,
                String initializer,
                boolean primitiveBoolean,
                boolean array,
                List<String> annotations) {
            this.name = Objects.requireNonNull(name);
            this.typeSource = Objects.requireNonNull(typeSource);
            this.initializer = initializer;
            this.primitiveBoolean = primitiveBoolean;
            this.array = array;
            this.annotations = Collections.unmodifiableList(new ArrayList<String>(
                    annotations == null ? Collections.<String>emptyList() : annotations));
        }

        static AssembledProperty fromType(String name, TypeMirror type, String initializer) {
            return fromType(name, type, Collections.<TypeMirror>emptyList(), initializer,
                    Collections.<String>emptyList());
        }

        static AssembledProperty fromType(
                String name,
                TypeMirror type,
                List<TypeMirror> typeArgs,
                String initializer,
                List<String> annotations) {
            String rendered = renderType(type);
            if (typeArgs != null && !typeArgs.isEmpty()) {
                StringBuilder generic = new StringBuilder(rendered).append('<');
                for (int i = 0; i < typeArgs.size(); i++) {
                    if (i > 0) {
                        generic.append(", ");
                    }
                    generic.append(renderType(typeArgs.get(i)));
                }
                generic.append('>');
                rendered = generic.toString();
            }
            return new AssembledProperty(
                    name,
                    rendered,
                    initializer,
                    type.getKind() == TypeKind.BOOLEAN,
                    type.getKind() == TypeKind.ARRAY,
                    annotations);
        }

        static AssembledProperty fromSource(
                String name,
                String typeSource,
                String initializer,
                List<String> annotations) {
            String trimmed = typeSource.trim();
            return new AssembledProperty(
                    name,
                    trimmed,
                    initializer,
                    "boolean".equals(trimmed),
                    isArrayTypeSource(trimmed),
                    annotations);
        }

        static boolean isArrayTypeSource(String typeSource) {
            return stripTopLevelGenerics(typeSource).endsWith("[]");
        }

        AssembledProperty mergeWith(AssembledProperty other) {
            String mergedInitializer = this.initializer != null ? this.initializer : other.initializer;
            List<String> mergedAnnotations = new ArrayList<String>(this.annotations);
            for (String annotation : other.annotations) {
                if (!mergedAnnotations.contains(annotation)) {
                    mergedAnnotations.add(annotation);
                }
            }
            return new AssembledProperty(
                    name, typeSource, mergedInitializer, primitiveBoolean, array, mergedAnnotations);
        }

        AssembledProperty withoutAnnotationTypes(List<String> removedTypes) {
            if (removedTypes == null || removedTypes.isEmpty() || annotations.isEmpty()) {
                return this;
            }
            List<String> kept = new ArrayList<String>();
            for (String annotation : annotations) {
                if (!removedTypes.contains(annotationTypeName(annotation))) {
                    kept.add(annotation);
                }
            }
            if (kept.size() == annotations.size()) {
                return this;
            }
            return new AssembledProperty(name, typeSource, initializer, primitiveBoolean, array, kept);
        }

        static String annotationTypeName(String rendered) {
            String type = rendered.startsWith("@") ? rendered.substring(1) : rendered;
            int paren = type.indexOf('(');
            if (paren >= 0) {
                type = type.substring(0, paren);
            }
            return type.trim();
        }

        String name() {
            return name;
        }

        String typeSource() {
            return typeSource;
        }

        String initializer() {
            return initializer;
        }

        boolean primitiveBoolean() {
            return primitiveBoolean;
        }

        boolean array() {
            return array;
        }

        List<String> annotations() {
            return annotations;
        }

        String getter() {
            String prefix = primitiveBoolean ? "is" : "get";
            return ParametersProcessor.accessor(prefix, name);
        }

        String setter() {
            return ParametersProcessor.accessor("set", name);
        }

        static String renderType(TypeMirror type) {
            switch (type.getKind()) {
                case BOOLEAN:
                    return "boolean";
                case BYTE:
                    return "byte";
                case SHORT:
                    return "short";
                case INT:
                    return "int";
                case LONG:
                    return "long";
                case CHAR:
                    return "char";
                case FLOAT:
                    return "float";
                case DOUBLE:
                    return "double";
                case ARRAY:
                    return renderType(((ArrayType) type).getComponentType()) + "[]";
                default:
                    return stripJavaLang(type.toString());
            }
        }

        private static String stripJavaLang(String typeName) {
            return typeName.replaceAll("\\bjava\\.lang\\.([A-Za-z_][A-Za-z0-9_]*)(?![.\\w])", "$1");
        }
    }
}
