package com.tscript.tscriptc.analyze;

import com.tscript.tscriptc.analyze.scoping.*;
import com.tscript.tscriptc.analyze.structures.Symbol;
import com.tscript.tscriptc.analyze.structures.Visibility;
import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.Errors;
import com.tscript.tscriptc.utils.TreeScanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DefinitionResolver {

    public static Scope resolve(Tree tree) {
        Resolver resolver = new Resolver();
        tree.accept(resolver);
        return resolver.root;
    }


    private static class Resolver extends TreeScanner<BaseScope, Void> {

        private final BaseScope root = new GlobalScopeBase();

        @Override
        public Void visitRoot(RootTree node, BaseScope unused) {
            return super.visitRoot(node, root);
        }

        @Override
        public Void visitClass(ClassTree node, BaseScope scope) {
            putIfAbsent(node, scope, new Symbol(node.getName(), visibility(node), node, scope, Symbol.Kind.CLASS, isStatic(node)));
            scope = new ClassScopeBase(node.getName(), node, scope);
            return super.visitClass(node, scope);
        }

        @Override
        public Void visitNamespace(NamespaceTree node, BaseScope scope) {
            putIfAbsent(node, scope, new Symbol(node.getName(), visibility(node), node, scope, Symbol.Kind.NAMESPACE, isStatic(node)));
            scope = new NamespaceScopeBase(node, scope);
            return super.visitNamespace(node, scope);
        }

        @Override
        public Void visitFunction(FunctionTree node, BaseScope scope) {
            putIfAbsent(node, scope, new Symbol(node.getName(), visibility(node), node, scope, Symbol.Kind.FUNCTION, isStatic(node)));
            scope = new FunctionScopeBase(node, scope);
            return super.visitFunction(node, scope);
        }

        @Override
        public Void visitTryCatch(TryCatchTree node, BaseScope scope) {
            scan(node.getTryStatement(), scope);

            scope = new BlockScopeBase(node, scope);
            VarDefTree exVar = node.getExceptionVariable();
            putIfAbsent(exVar, scope, new Symbol(exVar.getName(), null, node, scope, Symbol.Kind.VARIABLE, false));

            scan(node.getCatchStatement(), scope);
            return null;
        }

        @Override
        public Void visitDoWhileLoop(DoWhileTree node, BaseScope scope) {
            return super.visitDoWhileLoop(node, scope);
        }

        private Visibility visibilityOfField = null;
        private boolean isStaticField = false;
        private boolean isConst = false;

        @Override
        public Void visitVarDefs(VarDefsTree node, BaseScope scope) {
            Visibility prevVis = visibilityOfField;
            visibilityOfField = visibility(node);
            isStaticField = isStatic(node);
            isConst = isConst(node);
            super.visitVarDefs(node, scope);
            this.visibilityOfField = prevVis;
            isStaticField = false;
            isConst = false;
            return null;
        }

        @Override
        public Void visitVarDef(VarDefTree node, BaseScope scope) {
            Symbol.Kind kind = isConst ? Symbol.Kind.CONSTANT : Symbol.Kind.VARIABLE;
            putIfAbsent(node, scope, new Symbol(node.getName(), visibilityOfField, node, scope, kind, isStaticField));
            return super.visitVarDef(node, scope);
        }


        @Override
        public Void visitParameter(ParameterTree node, BaseScope scope) {
            Symbol.Kind kind = isConst ? Symbol.Kind.CONSTANT : Symbol.Kind.VARIABLE;
            putIfAbsent(node, scope, new Symbol(node.getName(), visibilityOfField, node, scope, kind, isStaticField));
            return super.visitParameter(node, scope);
        }

        @Override
        public Void visitConstructor(ConstructorTree node, BaseScope scope) {
            scope = new FunctionScopeBase(node, scope);
            return super.visitConstructor(node, scope);
        }

        @Override
        public Void visitImport(ImportTree node, BaseScope scope) {
            String name = node.getAccessChain().get(node.getAccessChain().size() - 1);
            putIfAbsent(node, scope, new Symbol(name, null, node, scope, Symbol.Kind.UNKNOWN, false));
            return null;
        }

        @Override
        public Void visitFromImport(FromImportTree node, BaseScope scope) {
            String name = node.getImportAccessChain().get(node.getImportAccessChain().size() - 1);
            putIfAbsent(node, scope, new Symbol(name, null, node, scope, Symbol.Kind.UNKNOWN, false));
            return null;
        }

        @Override
        public Void visitBlock(BlockTree node, BaseScope scope) {
            scope = new BlockScopeBase(node, scope);
            return super.visitBlock(node, scope);
        }

        @Override
        public Void visitLambda(LambdaTree node, BaseScope scope) {
            BaseScope lambdaScope = new LambdaScopeBase(scope.global);
            scope.children.put(node, lambdaScope);
            return super.visitLambda(node, lambdaScope);
        }

        @Override
        public Void visitForLoop(ForLoopTree node, BaseScope scope) {
            scope = new BlockScopeBase(node, scope);
            if (node.getVariable() != null){
                putIfAbsent(node, scope, new Symbol(node.getVariable().getName(), null, node, scope, Symbol.Kind.VARIABLE, false));
            }
            return scan(node.getStatement(), scope);
        }

        private static void putIfAbsent(Tree caller, BaseScope scope, Symbol symbol) {

            boolean alreadyExists = scope.accept(new ScopeVisitor<>() {

                @Override
                public Boolean visitBlock(BlockScope scope, String s) {
                    if (scope.hasSymbol(s)) return true;
                    return scope.getEnclosingScope().accept(this, s);
                }

                @Override
                public Boolean visitClass(ClassScope scope, String s) {
                    if (scope.hasSymbol(s)) return true;
                    return scope.getEnclosingScope().accept(this, s);
                }

                @Override
                public Boolean visitFunction(FunctionScope scope, String s) {
                    if (scope.hasSymbol(s)) return true;
                    return scope.getEnclosingScope().accept(this, s);
                }

                @Override
                public Boolean visitGlobal(GlobalScope scope, String s) {
                    return scope.hasSymbol(s);
                }

                @Override
                public Boolean visitLambda(LambdaScope scope, String s) {
                    return scope.hasSymbol(s);
                }

                @Override
                public Boolean visitNamespace(NamespaceScope scope, String s) {
                    if (scope.hasSymbol(s)) return true;
                    return scope.getEnclosingScope().accept(this, s);
                }

                @Override
                public Boolean visitExternal(ExternalScope scope, String s) {
                    throw new AssertionError();
                }

            }, symbol.name);


            if (alreadyExists)
                throw Errors.alreadyDefinedError(symbol.name, caller.getLocation());


            scope.content.put(symbol.name, symbol);
        }

        private static Visibility visibility(Tree tree) {
            if (tree instanceof ClassMemberTree mem){
                Set<Modifier> modifiers = mem.getModifiers().getModifiers();
                if (modifiers.contains(Modifier.PUBLIC))return Visibility.PUBLIC;
                if (modifiers.contains(Modifier.PROTECTED))return Visibility.PROTECTED;
                if (modifiers.contains(Modifier.PRIVATE))return Visibility.PRIVATE;
            }
            return null;
        }

        private static boolean isStatic(Tree tree) {
            return tree instanceof ClassMemberTree mem
                    && mem.getModifiers().getModifiers().contains(Modifier.STATIC);
        }

        private static boolean isConst(Tree tree) {
            return tree instanceof ClassMemberTree mem
                    && mem.getModifiers().getModifiers().contains(Modifier.CONSTANT);
        }

    }


    private static abstract class BaseScope implements Scope {

        Scope global;
        public final Map<String, Symbol> content;
        public final Map<Object, Scope> children;

        private BaseScope(Scope global) {
            this.global = global;
            this.content = new HashMap<>();
            this.children = new HashMap<>();
        }

        @Override
        public Scope getChildScope(Object key) {
            return Objects.requireNonNull(children.get(key));
        }

        @Override
        public Scope getGlobalScope() {
            return global;
        }

        @Override
        public Symbol getSymbol(String name) {
            return content.get(name);
        }

        @Override
        public abstract  <P, R> R accept(ScopeVisitor<P, R> visitor, P param);

    }


    private static abstract class BaseNestedScope extends BaseScope implements NestedScope {

        private final Scope enclosing;

        private BaseNestedScope(Object key, BaseScope enclosing) {
            super(enclosing.getGlobalScope());
            this.enclosing = enclosing;
            enclosing.children.put(key, this);
        }

        @Override
        public Scope getEnclosingScope() {
            return enclosing;
        }

        public abstract <P, R> R accept(ScopeVisitor<P, R> visitor, P param);
    }


    private static class BlockScopeBase extends BaseNestedScope implements BlockScope {

        private BlockScopeBase(Object key, BaseScope enclosing) {
            super(key, enclosing);
        }

        @Override
        public <P, R> R accept(ScopeVisitor<P, R> visitor, P param) {
            return visitor.visitBlock(this, param);
        }
    }

    public static class ClassScopeBase extends BaseNestedScope implements ClassScope {

        public String className;
        public Scope superScope;

        private ClassScopeBase(String className, Object key, BaseScope enclosing) {
            super(key, enclosing);
            this.className = className;
        }

        @Override
        public <P, R> R accept(ScopeVisitor<P, R> visitor, P param) {
            return visitor.visitClass(this, param);
        }

        @Override
        public String getClassName() {
            return className;
        }

        @Override
        public Scope getSuperClassScope() {
            return superScope;
        }
    }

    private static class FunctionScopeBase extends BaseNestedScope implements FunctionScope {

        private FunctionScopeBase(Object key, BaseScope enclosing) {
            super(key, enclosing);
        }

        @Override
        public <P, R> R accept(ScopeVisitor<P, R> visitor, P param) {
            return visitor.visitFunction(this, param);
        }
    }

    private static class GlobalScopeBase extends BaseScope implements GlobalScope {

        private GlobalScopeBase() {
            super(null);
            super.global = this;
        }

        @Override
        public <P, R> R accept(ScopeVisitor<P, R> visitor, P param) {
            return visitor.visitGlobal(this, param);
        }
    }

    private static class LambdaScopeBase extends BaseScope implements LambdaScope {

        private LambdaScopeBase(Scope global) {
            super(global);
        }

        @Override
        public <P, R> R accept(ScopeVisitor<P, R> visitor, P param) {
            return visitor.visitLambda(this, param);
        }
    }

    private static class NamespaceScopeBase extends BaseNestedScope implements NamespaceScope {

        private NamespaceScopeBase(Object key, BaseScope enclosing) {
            super(key, enclosing);
        }

        @Override
        public <P, R> R accept(ScopeVisitor<P, R> visitor, P param) {
            return visitor.visitNamespace(this, param);
        }
    }


}
