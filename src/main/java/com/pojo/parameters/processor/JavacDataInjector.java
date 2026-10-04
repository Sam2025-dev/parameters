package com.pojo.parameters.processor;

import com.pojo.parameters.handlers.HandleData;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Javac AST helpers used by {@code com.pojo.parameters.handlers}, analogous to
 * {@code lombok.javac.handlers.JavacHandlerUtil}.
 */
public final class JavacDataInjector {

    public static final java.util.List<String> INTEGRAL_PRIMITIVES =
            Arrays.asList("byte", "short", "int", "long", "char");

    private static final java.util.List<JCMethodDecl> GENERATED_METHODS =
            new ArrayList<JCMethodDecl>();

    private final TreeMaker maker;
    private final Names names;
    private final ParserFactory parsers;
    private final Context context;
    private final Enter enter;
    private final Attr attr;
    private final Symtab syms;

    JavacDataInjector(ProcessingEnvironment processingEnv) {
        this.context = javacContext(processingEnv);
        this.maker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        this.parsers = ParserFactory.instance(context);
        this.enter = Enter.instance(context);
        this.attr = Attr.instance(context);
        this.syms = Symtab.instance(context);
    }

    void inject(TypeElement type, JCClassDecl classDecl, AssembledProperties properties) {
        maker.at(classDecl.pos);
        new HandleData().handle(this, type, classDecl, properties);
    }

    public TreeMaker maker() {
        return maker;
    }

    public Names names() {
        return names;
    }

    public JCExpression parseType(String source) {
        Parser parser = parsers.newParser(source, false, false, false);
        return parser.parseType();
    }

    public JCExpression parseExpression(String source) {
        Parser parser = parsers.newParser(source, false, false, false);
        return parser.parseExpression();
    }

    public JCExpression thisField(String fieldName) {
        return maker.Select(maker.Ident(names._this), names.fromString(fieldName));
    }

    public JCExpression qualid(String fqn) {
        String[] parts = fqn.split("\\.");
        JCExpression expr = maker.Ident(names.fromString(parts[0]));
        for (int i = 1; i < parts.length; i++) {
            expr = maker.Select(expr, names.fromString(parts[i]));
        }
        return expr;
    }

    public JCTree.JCModifiers publicOverride(boolean override) {
        if (!override) {
            return maker.Modifiers(Flags.PUBLIC);
        }
        JCAnnotation annotation = maker.Annotation(qualid("java.lang.Override"), List.<JCExpression>nil());
        return maker.Modifiers(Flags.PUBLIC, List.of(annotation));
    }

    public void injectField(JCClassDecl classDecl, JCVariableDecl field) {
        Type type = attribType(field.vartype, classDecl);
        field.type = type;
        field.vartype.type = type;
        VarSymbol symbol = new VarSymbol(Flags.PRIVATE, field.name, type, classDecl.sym);
        field.sym = symbol;
        classDecl.defs = classDecl.defs.append(field);
        classDecl.sym.members().enter(symbol);
    }

    public void injectMethod(JCClassDecl classDecl, JCMethodDecl method, long flags) {
        Type restype = method.restype == null ? syms.voidType : attribType(method.restype, classDecl);
        if (method.restype != null) {
            method.restype.type = restype;
        }
        enterMethod(classDecl, method, flags, restype);
    }

    public void injectConstructor(JCClassDecl classDecl, JCMethodDecl method) {
        enterMethod(classDecl, method, Flags.PUBLIC, syms.voidType);
    }

    private void enterMethod(JCClassDecl classDecl, JCMethodDecl method, long flags, Type restype) {
        ListBuffer<Type> argTypes = new ListBuffer<Type>();
        for (JCVariableDecl param : method.params) {
            Type ptype = attribType(param.vartype, classDecl);
            param.type = ptype;
            param.vartype.type = ptype;
            argTypes.append(ptype);
        }
        MethodType methodType = new MethodType(argTypes.toList(), restype, List.<Type>nil(), syms.methodClass);
        MethodSymbol methodSymbol = new MethodSymbol(flags, method.name, methodType, classDecl.sym);
        method.sym = methodSymbol;
        method.type = methodType;
        ListBuffer<VarSymbol> paramSymbols = new ListBuffer<VarSymbol>();
        int adr = 0;
        ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
        for (JCVariableDecl param : method.params) {
            VarSymbol paramSymbol = new VarSymbol(Flags.PARAMETER | Flags.FINAL, param.name, param.type, methodSymbol);
            paramSymbol.adr = adr++;
            param.sym = paramSymbol;
            params.append(param);
            paramSymbols.append(paramSymbol);
        }
        method.params = params.toList();
        methodSymbol.params = paramSymbols.toList();
        classDecl.defs = classDecl.defs.append(method);
        classDecl.sym.members().enter(methodSymbol);
        GENERATED_METHODS.add(method);
    }

    static void fixParameterAddresses() {
        for (JCMethodDecl method : GENERATED_METHODS) {
            if (method.params == null) {
                continue;
            }
            int adr = 0;
            for (JCVariableDecl param : method.params) {
                if (param.sym != null) {
                    param.sym.adr = adr;
                }
                adr++;
            }
        }
    }

    private Type attribType(JCExpression tree, JCClassDecl classDecl) {
        Env<AttrContext> env = enter.getEnv(classDecl.sym);
        return attr.attribType(tree, env);
    }

    public boolean hasField(JCClassDecl classDecl, String name) {
        for (JCTree def : classDecl.defs) {
            if (def instanceof JCVariableDecl
                    && ((JCVariableDecl) def).getName().contentEquals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFinalField(JCClassDecl classDecl, String name) {
        for (JCTree def : classDecl.defs) {
            if (def instanceof JCVariableDecl
                    && ((JCVariableDecl) def).getName().contentEquals(name)) {
                return (((JCVariableDecl) def).mods.flags & Flags.FINAL) != 0;
            }
        }
        return false;
    }

    public boolean hasMethod(JCClassDecl classDecl, String name, int paramCount) {
        for (JCTree def : classDecl.defs) {
            if (!(def instanceof JCMethodDecl)) {
                continue;
            }
            JCMethodDecl method = (JCMethodDecl) def;
            if (method.getName().contentEquals(name) && method.getParameters().size() == paramCount) {
                return true;
            }
        }
        return false;
    }

    public boolean hasConstructor(JCClassDecl classDecl) {
        for (JCTree def : classDecl.defs) {
            if (def instanceof JCMethodDecl && ((JCMethodDecl) def).name == names.init) {
                return true;
            }
        }
        return false;
    }

    static boolean isJavac(ProcessingEnvironment processingEnv) {
        return javacContext(processingEnv) != null;
    }

    static JCClassDecl classDecl(ProcessingEnvironment processingEnv, Element type) {
        try {
            Object trees = Class.forName("com.sun.source.util.Trees")
                    .getMethod("instance", ProcessingEnvironment.class)
                    .invoke(null, processingEnv);
            Object tree = trees.getClass().getMethod("getTree", Element.class).invoke(trees, type);
            if (tree instanceof JCClassDecl) {
                return (JCClassDecl) tree;
            }
        } catch (Exception ignored) {
            // fall through
        }
        return null;
    }

    private static Context javacContext(ProcessingEnvironment processingEnv) {
        Object env = processingEnv;
        for (int i = 0; i < 8 && env != null; i++) {
            if (env instanceof JavacProcessingEnvironment) {
                return ((JavacProcessingEnvironment) env).getContext();
            }
            try {
                Method getContext = env.getClass().getMethod("getContext");
                Object context = getContext.invoke(env);
                if (context instanceof Context) {
                    return (Context) context;
                }
            } catch (Exception ignored) {
                // try unwrap
            }
            env = unwrap(env);
        }
        return null;
    }

    private static Object unwrap(Object env) {
        for (String fieldName : new String[] {"delegate", "processingEnv", "env"}) {
            Class<?> type = env.getClass();
            while (type != null) {
                try {
                    Field field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.get(env);
                } catch (NoSuchFieldException ignored) {
                    type = type.getSuperclass();
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
