package com.pojo.parameters.handlers;

import com.pojo.parameters.processor.AssembledProperties;
import com.pojo.parameters.processor.AssembledProperties.AssembledProperty;
import com.pojo.parameters.processor.JavacDataInjector;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

public final class HandleGetter {

    public void generateGetterForType(JavacDataInjector javac, JCClassDecl classDecl, AssembledProperties properties) {
        for (AssembledProperty property : properties.properties()) {
            if (!HandlerUtil.fieldQualifies(property.name(), false)) {
                continue;
            }
            String getter = property.getter();
            if (javac.hasMethod(classDecl, getter, 0)) {
                continue;
            }
            JCStatement body = javac.maker().Return(javac.thisField(property.name()));
            JCMethodDecl method = javac.maker().MethodDef(
                    javac.maker().Modifiers(Flags.PUBLIC),
                    javac.names().fromString(getter),
                    javac.parseType(property.typeSource()),
                    List.<JCTree.JCTypeParameter>nil(),
                    List.<JCVariableDecl>nil(),
                    List.<JCExpression>nil(),
                    javac.maker().Block(0, List.of(body)),
                    null);
            javac.injectMethod(classDecl, method, Flags.PUBLIC);
        }
    }
}
