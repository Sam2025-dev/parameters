package com.pojo.parameters.handlers;

import com.pojo.parameters.processor.AssembledProperties;
import com.pojo.parameters.processor.AssembledProperties.AssembledProperty;
import com.pojo.parameters.processor.JavacDataInjector;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

/**
 * Injects {@code @Of} properties as private instance fields onto the annotated class.
 */
public final class HandleOf {

    public void generateFields(JavacDataInjector javac, JCClassDecl classDecl, AssembledProperties properties) {
        for (AssembledProperty property : properties.properties()) {
            if (!property.generateField()) {
                continue;
            }
            if (!HandlerUtil.fieldQualifies(property.name(), false)) {
                continue;
            }
            if (javac.hasField(classDecl, property.name())) {
                continue;
            }
            JCExpression init = null;
            if (property.initializer() != null) {
                init = javac.parseExpression(property.initializer());
            }
            JCVariableDecl field = javac.maker().VarDef(
                    javac.maker().Modifiers(Flags.PRIVATE),
                    javac.names().fromString(property.name()),
                    javac.parseType(property.typeSource()),
                    init);
            javac.injectField(classDecl, field);
        }
    }
}
