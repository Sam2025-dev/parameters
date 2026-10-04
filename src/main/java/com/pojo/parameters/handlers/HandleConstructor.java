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
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

/**
 * Required-args and extra no-args constructors, as in {@code lombok.javac.handlers.HandleConstructor}.
 */
public final class HandleConstructor {

    public boolean generateRequiredArgsConstructor(
            JavacDataInjector javac, JCClassDecl classDecl, AssembledProperties properties) {
        if (javac.hasConstructor(classDecl)) {
            return false;
        }
        ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
        ListBuffer<JCStatement> assigns = new ListBuffer<JCStatement>();
        for (AssembledProperty property : properties.properties()) {
            if (!HandlerUtil.fieldQualifies(property.name(), false)) {
                continue;
            }
            if (!javac.isFinalField(classDecl, property.name())) {
                continue;
            }
            Name name = javac.names().fromString(property.name());
            JCExpression type = javac.parseType(property.typeSource());
            params.append(javac.maker().VarDef(
                    javac.maker().Modifiers(Flags.PARAMETER | Flags.FINAL),
                    name,
                    type,
                    null));
            assigns.append(javac.maker().Exec(javac.maker().Assign(
                    javac.maker().Select(javac.maker().Ident(javac.names()._this), name),
                    javac.maker().Ident(name))));
        }
        if (params.isEmpty()) {
            return false;
        }
        JCMethodDecl constructor = javac.maker().MethodDef(
                javac.maker().Modifiers(Flags.PUBLIC),
                javac.names().fromString("<init>"),
                null,
                List.<JCTree.JCTypeParameter>nil(),
                params.toList(),
                List.<JCExpression>nil(),
                javac.maker().Block(0, assigns.toList()),
                null);
        javac.injectConstructor(classDecl, constructor);
        return true;
    }

    public void generateExtraNoArgsConstructor(JavacDataInjector javac, JCClassDecl classDecl) {
        if (javac.hasConstructor(classDecl)) {
            return;
        }
        JCMethodDecl constructor = javac.maker().MethodDef(
                javac.maker().Modifiers(Flags.PUBLIC),
                javac.names().fromString("<init>"),
                null,
                List.<JCTree.JCTypeParameter>nil(),
                List.<JCVariableDecl>nil(),
                List.<JCExpression>nil(),
                javac.maker().Block(0, List.<JCStatement>nil()),
                null);
        javac.injectConstructor(classDecl, constructor);
    }
}
