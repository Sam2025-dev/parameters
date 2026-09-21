package com.pojo.parameters.processor;

import com.pojo.parameters.Parameters;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
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
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Assembles {@link Parameters} fields into a generated superclass at compile time.
 * That superclass may extend {@link Parameters#Extends()} and implement
 * {@link Parameters#Implements()}.
 */
@SupportedAnnotationTypes("com.pojo.parameters.Parameters")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class ParametersProcessor extends AbstractProcessor {

    static final String SUPERCLASS_SUFFIX = "__Parameters";

    private final Set<String> written = new LinkedHashSet<>();
    private Trees trees;
    private boolean treesResolved;

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
        AssembledProperties properties = AssembledProperties.from(
                type,
                annotation,
                parametersMirror(type),
                processingEnv.getElementUtils(),
                this::fieldInitializer,
                this::error);
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

    private AnnotationMirror parametersMirror(TypeElement type) {
        for (AnnotationMirror mirror : type.getAnnotationMirrors()) {
            if (Parameters.class.getCanonicalName().equals(mirror.getAnnotationType().toString())) {
                return mirror;
            }
        }
        return null;
    }

    static String packageName(TypeElement type) {
        String qualified = type.getQualifiedName().toString();
        int lastDot = qualified.lastIndexOf('.');
        return lastDot < 0 ? "" : qualified.substring(0, lastDot);
    }

    static String accessor(String prefix, String fieldName) {
        return prefix + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    void error(Element element, String message) {
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private String fieldInitializer(VariableElement field) {
        Object constant = field.getConstantValue();
        if (constant != null) {
            return AssembledProperties.renderConstant(constant);
        }
        Trees ast = trees();
        if (ast == null) {
            return null;
        }
        try {
            TreePath path = ast.getPath(field);
            if (path == null || !(path.getLeaf() instanceof VariableTree)) {
                return null;
            }
            ExpressionTree init = ((VariableTree) path.getLeaf()).getInitializer();
            if (init == null) {
                return null;
            }
            String source = init.toString().trim();
            return source.isEmpty() ? null : source;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private Trees trees() {
        if (!treesResolved) {
            treesResolved = true;
            try {
                trees = Trees.instance(processingEnv);
            } catch (IllegalArgumentException ignored) {
                trees = null;
            }
        }
        return trees;
    }
}
