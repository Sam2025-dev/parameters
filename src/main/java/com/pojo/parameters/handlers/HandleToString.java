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

import javax.lang.model.element.TypeElement;

public final class HandleToString {

    public void generateToStringForType(
            JavacDataInjector javac, TypeElement type, JCClassDecl classDecl, AssembledProperties properties) {
        if (javac.hasMethod(classDecl, "toString", 0)) {
            return;
        }
        String className = type.getSimpleName().toString();
        JCExpression expr = javac.maker().Literal(className + "{");
        int i = 0;
        for (AssembledProperty property : properties.properties()) {
            if (!HandlerUtil.fieldQualifies(property.name(), false)) {
                continue;
            }
            String prefix = (i == 0 ? "" : ", ") + property.name() + "=";
            JCExpression value = property.array()
                    ? javac.maker().Apply(
                            List.<JCExpression>nil(),
                            javac.maker().Select(javac.qualid("java.util.Arrays"), javac.names().fromString("toString")),
                            List.of(javac.thisField(property.name())))
                    : javac.thisField(property.name());
            expr = javac.maker().Binary(JCTree.Tag.PLUS, expr, javac.maker().Literal(prefix));
            expr = javac.maker().Binary(JCTree.Tag.PLUS, expr, value);
            i++;
        }
        expr = javac.maker().Binary(JCTree.Tag.PLUS, expr, javac.maker().Literal("}"));
        JCMethodDecl method = javac.maker().MethodDef(
                javac.publicOverride(true),
                javac.names().fromString("toString"),
                javac.qualid("java.lang.String"),
                List.<JCTree.JCTypeParameter>nil(),
                List.<JCVariableDecl>nil(),
                List.<JCExpression>nil(),
                javac.maker().Block(0, List.of((JCStatement) javac.maker().Return(expr))),
                null);
        javac.injectMethod(classDecl, method, Flags.PUBLIC);
    }
}
