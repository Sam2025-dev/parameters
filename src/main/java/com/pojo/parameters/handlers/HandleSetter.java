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
import com.sun.tools.javac.util.Name;

public final class HandleSetter {

    public void generateSetterForType(JavacDataInjector javac, JCClassDecl classDecl, AssembledProperties properties) {
        for (AssembledProperty property : properties.properties()) {
            if (!HandlerUtil.fieldQualifies(property.name(), false)) {
                continue;
            }
            if (javac.isFinalField(classDecl, property.name())) {
                continue;
            }
            String setter = property.setter();
            if (javac.hasMethod(classDecl, setter, 1)) {
                continue;
            }
            Name name = javac.names().fromString(property.name());
            JCVariableDecl param = javac.maker().VarDef(
                    javac.maker().Modifiers(Flags.PARAMETER | Flags.FINAL),
                    name,
                    javac.parseType(property.typeSource()),
                    null);
            JCStatement assign = javac.maker().Exec(javac.maker().Assign(
                    javac.maker().Select(javac.maker().Ident(javac.names()._this), name),
                    javac.maker().Ident(name)));
            JCMethodDecl method = javac.maker().MethodDef(
                    javac.maker().Modifiers(Flags.PUBLIC),
                    javac.names().fromString(setter),
                    javac.maker().TypeIdent(com.sun.tools.javac.code.TypeTag.VOID),
                    List.<JCTree.JCTypeParameter>nil(),
                    List.of(param),
                    List.<JCExpression>nil(),
                    javac.maker().Block(0, List.of(assign)),
                    null);
            javac.injectMethod(classDecl, method, Flags.PUBLIC);
        }
    }
}
