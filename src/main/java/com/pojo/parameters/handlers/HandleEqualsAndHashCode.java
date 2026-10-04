package com.pojo.parameters.handlers;

import com.pojo.parameters.processor.AssembledProperties;
import com.pojo.parameters.processor.AssembledProperties.AssembledProperty;
import com.pojo.parameters.processor.JavacDataInjector;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.lang.model.element.TypeElement;

public final class HandleEqualsAndHashCode {

    public void generateEqualsAndHashCodeForType(
            JavacDataInjector javac, TypeElement type, JCClassDecl classDecl, AssembledProperties properties) {
        if (!javac.hasMethod(classDecl, "equals", 1)) {
            javac.injectMethod(classDecl, equalsDecl(javac, type.getSimpleName().toString(), properties), Flags.PUBLIC);
        }
        if (!javac.hasMethod(classDecl, "hashCode", 0)) {
            javac.injectMethod(classDecl, hashCodeDecl(javac, properties), Flags.PUBLIC);
        }
    }

    private JCMethodDecl equalsDecl(JavacDataInjector javac, String className, AssembledProperties properties) {
        ListBuffer<JCStatement> stats = new ListBuffer<JCStatement>();
        stats.append(javac.maker().If(
                javac.maker().Binary(JCTree.Tag.EQ, javac.maker().Ident(javac.names()._this), javac.maker().Ident(javac.names().fromString("o"))),
                javac.maker().Return(javac.maker().Literal(true)),
                null));
        stats.append(javac.maker().If(
                javac.maker().Unary(JCTree.Tag.NOT, javac.maker().TypeTest(
                        javac.maker().Ident(javac.names().fromString("o")),
                        javac.maker().Ident(javac.names().fromString(className)))),
                javac.maker().Return(javac.maker().Literal(false)),
                null));
        stats.append(javac.maker().VarDef(
                javac.maker().Modifiers(Flags.FINAL),
                javac.names().fromString("that"),
                javac.maker().Ident(javac.names().fromString(className)),
                javac.maker().TypeCast(javac.maker().Ident(javac.names().fromString(className)), javac.maker().Ident(javac.names().fromString("o")))));
        if (properties.isEmpty()) {
            stats.append(javac.maker().Return(javac.maker().Literal(true)));
        } else {
            JCExpression expr = null;
            for (AssembledProperty property : properties.properties()) {
                if (!HandlerUtil.fieldQualifies(property.name(), false)) {
                    continue;
                }
                JCExpression part = equalsPart(javac, property);
                expr = expr == null ? part : javac.maker().Binary(JCTree.Tag.AND, expr, part);
            }
            stats.append(javac.maker().Return(expr == null ? javac.maker().Literal(true) : expr));
        }
        JCVariableDecl param = javac.maker().VarDef(
                javac.maker().Modifiers(Flags.PARAMETER | Flags.FINAL),
                javac.names().fromString("o"),
                javac.qualid("java.lang.Object"),
                null);
        return javac.maker().MethodDef(
                javac.publicOverride(true),
                javac.names().fromString("equals"),
                javac.maker().TypeIdent(TypeTag.BOOLEAN),
                List.<JCTree.JCTypeParameter>nil(),
                List.of(param),
                List.<JCExpression>nil(),
                javac.maker().Block(0, stats.toList()),
                null);
    }

    private JCExpression equalsPart(JavacDataInjector javac, AssembledProperty property) {
        JCExpression left = javac.thisField(property.name());
        JCExpression right = javac.maker().Select(javac.maker().Ident(javac.names().fromString("that")), javac.names().fromString(property.name()));
        if (property.array()) {
            return javac.maker().Apply(
                    List.<JCExpression>nil(),
                    javac.maker().Select(javac.qualid("java.util.Arrays"), javac.names().fromString("equals")),
                    List.of(left, right));
        }
        if ("float".equals(property.typeSource())) {
            return javac.maker().Binary(
                    JCTree.Tag.EQ,
                    javac.maker().Apply(
                            List.<JCExpression>nil(),
                            javac.maker().Select(javac.qualid("java.lang.Float"), javac.names().fromString("compare")),
                            List.of(left, right)),
                    javac.maker().Literal(0));
        }
        if ("double".equals(property.typeSource())) {
            return javac.maker().Binary(
                    JCTree.Tag.EQ,
                    javac.maker().Apply(
                            List.<JCExpression>nil(),
                            javac.maker().Select(javac.qualid("java.lang.Double"), javac.names().fromString("compare")),
                            List.of(left, right)),
                    javac.maker().Literal(0));
        }
        if (property.primitiveBoolean() || JavacDataInjector.INTEGRAL_PRIMITIVES.contains(property.typeSource())) {
            return javac.maker().Binary(JCTree.Tag.EQ, left, right);
        }
        return javac.maker().Apply(
                List.<JCExpression>nil(),
                javac.maker().Select(javac.qualid("java.util.Objects"), javac.names().fromString("equals")),
                List.of(left, right));
    }

    private JCMethodDecl hashCodeDecl(JavacDataInjector javac, AssembledProperties properties) {
        JCExpression expr;
        if (properties.isEmpty() || !hasQualifying(properties)) {
            expr = javac.maker().Literal(0);
        } else if (!hasArray(properties)) {
            ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
            for (AssembledProperty property : properties.properties()) {
                if (!HandlerUtil.fieldQualifies(property.name(), false)) {
                    continue;
                }
                args.append(javac.thisField(property.name()));
            }
            expr = javac.maker().Apply(
                    List.<JCExpression>nil(),
                    javac.maker().Select(javac.qualid("java.util.Objects"), javac.names().fromString("hash")),
                    args.toList());
        } else {
            expr = javac.maker().Literal(1);
            for (AssembledProperty property : properties.properties()) {
                if (!HandlerUtil.fieldQualifies(property.name(), false)) {
                    continue;
                }
                JCExpression piece = property.array()
                        ? javac.maker().Apply(
                                List.<JCExpression>nil(),
                                javac.maker().Select(javac.qualid("java.util.Arrays"), javac.names().fromString("hashCode")),
                                List.of(javac.thisField(property.name())))
                        : javac.maker().Apply(
                                List.<JCExpression>nil(),
                                javac.maker().Select(javac.qualid("java.util.Objects"), javac.names().fromString("hashCode")),
                                List.of(javac.thisField(property.name())));
                expr = javac.maker().Binary(JCTree.Tag.PLUS, javac.maker().Binary(JCTree.Tag.MUL, expr, javac.maker().Literal(31)), piece);
            }
        }
        return javac.maker().MethodDef(
                javac.publicOverride(true),
                javac.names().fromString("hashCode"),
                javac.maker().TypeIdent(TypeTag.INT),
                List.<JCTree.JCTypeParameter>nil(),
                List.<JCVariableDecl>nil(),
                List.<JCExpression>nil(),
                javac.maker().Block(0, List.of((JCStatement) javac.maker().Return(expr))),
                null);
    }

    private static boolean hasQualifying(AssembledProperties properties) {
        for (AssembledProperty property : properties.properties()) {
            if (HandlerUtil.fieldQualifies(property.name(), false)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasArray(AssembledProperties properties) {
        for (AssembledProperty property : properties.properties()) {
            if (HandlerUtil.fieldQualifies(property.name(), false) && property.array()) {
                return true;
            }
        }
        return false;
    }
}
