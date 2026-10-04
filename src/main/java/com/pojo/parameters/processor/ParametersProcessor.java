package com.pojo.parameters.processor;

import com.pojo.parameters.Data;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * Assembles {@link Data} members onto the annotated class, following Lombok {@code @Data}.
 */
@SupportedAnnotationTypes("com.pojo.parameters.Data")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class ParametersProcessor extends AbstractProcessor {

    private Trees trees;
    private boolean treesResolved;

    @Override
    public synchronized void init(javax.annotation.processing.ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        try {
            JavacTask.instance(processingEnv).addTaskListener(new TaskListener() {
                @Override
                public void started(TaskEvent event) {
                    if (event.getKind() == TaskEvent.Kind.ANALYZE) {
                        JavacDataInjector.fixParameterAddresses();
                    }
                }

                @Override
                public void finished(TaskEvent event) {
                }
            });
        } catch (RuntimeException ignored) {
            // compile-testing still provides javac Task
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(Data.class)) {
            processElement(element);
        }
        return false;
    }

    private void processElement(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            error(element, "@Data can only be placed on classes.");
            return;
        }
        TypeElement type = (TypeElement) element;
        AnnotationMirror mirror = assemblyMirror(type);
        if (mirror == null) {
            return;
        }
        AssembledProperties properties = AssembledProperties.from(
                type,
                mirror,
                processingEnv.getElementUtils(),
                this::fieldInitializer,
                this::error);
        if (properties == null) {
            return;
        }
        if (!JavacDataInjector.isJavac(processingEnv)) {
            error(type, "@Data requires javac (Lombok-style AST injection).");
            return;
        }
        JCClassDecl classDecl = classTree(type);
        if (classDecl == null) {
            error(type, "@Data could not read the class AST.");
            return;
        }
        try {
            new JavacDataInjector(processingEnv).inject(type, classDecl, properties);
        } catch (RuntimeException ex) {
            error(type, "Failed to generate @Data members: " + ex.getMessage());
        }
    }

    private JCClassDecl classTree(TypeElement type) {
        Trees ast = trees();
        if (ast == null) {
            return null;
        }
        TreePath path = ast.getPath(type);
        if (path == null || !(path.getLeaf() instanceof JCClassDecl)) {
            return null;
        }
        return (JCClassDecl) path.getLeaf();
    }

    private AnnotationMirror assemblyMirror(TypeElement type) {
        for (AnnotationMirror mirror : type.getAnnotationMirrors()) {
            if (Data.class.getCanonicalName().equals(mirror.getAnnotationType().toString())) {
                return mirror;
            }
        }
        return null;
    }

    static String accessor(String prefix, String fieldName) {
        return com.pojo.parameters.handlers.HandlerUtil.buildAccessorName(prefix, fieldName);
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
