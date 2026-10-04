package com.pojo.parameters.handlers;

import com.pojo.parameters.processor.AssembledProperties;
import com.pojo.parameters.processor.JavacDataInjector;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;

import javax.lang.model.element.TypeElement;

/**
 * Handles {@code com.pojo.parameters.Data}, including {@code of}/{@code @Of}.
 * Order matches {@code lombok.javac.handlers.HandleData}.
 */
public final class HandleData {

    private final HandleOf handleOf = new HandleOf();
    private final HandleConstructor handleConstructor = new HandleConstructor();
    private final HandleGetter handleGetter = new HandleGetter();
    private final HandleSetter handleSetter = new HandleSetter();
    private final HandleEqualsAndHashCode handleEqualsAndHashCode = new HandleEqualsAndHashCode();
    private final HandleToString handleToString = new HandleToString();

    public void handle(JavacDataInjector javac, TypeElement type, JCClassDecl classDecl, AssembledProperties properties) {
        handleOf.generateFields(javac, classDecl, properties);
        if (handleConstructor.generateRequiredArgsConstructor(javac, classDecl, properties)) {
            handleConstructor.generateExtraNoArgsConstructor(javac, classDecl);
        }
        handleGetter.generateGetterForType(javac, classDecl, properties);
        handleSetter.generateSetterForType(javac, classDecl, properties);
        handleEqualsAndHashCode.generateEqualsAndHashCodeForType(javac, type, classDecl, properties);
        handleToString.generateToStringForType(javac, type, classDecl, properties);
    }
}
