package com.tscript.tscriptc.analyze;

import com.tscript.tscriptc.analyze.structures.*;
import com.tscript.tscriptc.tree.*;
import com.tscript.tscriptc.utils.Errors;
import com.tscript.tscriptc.utils.TreeScanner;

import java.util.Set;

public class DefinitionResolver {

    public static Scope resolve(Tree tree) {
        Resolver resolver = new Resolver();
        tree.accept(resolver);
        return resolver.root;
    }


    private static class Resolver extends TreeScanner<Scope, Void> {

        private final Scope root = new Scope(null, Scope.Kind.GLOBAL, null);

        @Override
        public Void visitRoot(RootTree node, Scope unused) {
            return super.visitRoot(node, root);
        }

        @Override
        public Void visitClass(ClassTree node, Scope scope) {
            putIfAbsent(node, scope, new Symbol(node.getName(), visibility(node), node, Symbol.Kind.CLASS, isStatic(node)));
            scope = extend(node, scope, Scope.Kind.CLASS);
            return super.visitClass(node, scope);
        }

        @Override
        public Void visitNamespace(NamespaceTree node, Scope scope) {
            putIfAbsent(node, scope, new Symbol(node.getName(), visibility(node), node, Symbol.Kind.NAMESPACE, isStatic(node)));
            scope = extend(node, scope, Scope.Kind.NAMESPACE);
            return super.visitNamespace(node, scope);
        }

        @Override
        public Void visitFunction(FunctionTree node, Scope scope) {
            putIfAbsent(node, scope, new Symbol(node.getName(), visibility(node), node, Symbol.Kind.FUNCTION, isStatic(node)));
            scope = extend(node, scope, Scope.Kind.FUNCTION);
            return super.visitFunction(node, scope);
        }

        @Override
        public Void visitTryCatch(TryCatchTree node, Scope scope) {
            scan(node.getTryStatement(), scope);

            VarDefTree exVar = node.getExceptionVariable();
            putIfAbsent(exVar, scope, new Symbol(exVar.getName(), null, exVar, Symbol.Kind.VARIABLE, false));

            scan(node.getCatchStatement(), scope);
            return null;
        }

        private Visibility visibilityOfField = null;
        private boolean isStaticField = false;
        private boolean isConst = false;

        @Override
        public Void visitVarDefs(VarDefsTree node, Scope scope) {
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
        public Void visitVarDef(VarDefTree node, Scope scope) {
            Symbol.Kind kind = isConst ? Symbol.Kind.CONSTANT : Symbol.Kind.VARIABLE;
            putIfAbsent(node, scope, new Symbol(node.getName(), visibilityOfField, node, kind, isStaticField));
            return super.visitVarDef(node, scope);
        }

        @Override
        public Void visitConstructor(ConstructorTree node, Scope scope) {
            scope = extend(node, scope, Scope.Kind.CONSTRUCTOR);
            return super.visitConstructor(node, scope);
        }

        @Override
        public Void visitImport(ImportTree node, Scope scope) {
            String name = node.getAccessChain().get(node.getAccessChain().size() - 1);
            putIfAbsent(node, scope, new Symbol(name, null, node, Symbol.Kind.UNKNOWN, false));
            return null;
        }

        @Override
        public Void visitFromImport(FromImportTree node, Scope scope) {
            String name = node.getImportAccessChain().get(node.getImportAccessChain().size() - 1);
            putIfAbsent(node, scope, new Symbol(name, null, node, Symbol.Kind.UNKNOWN, false));
            return null;
        }

        @Override
        public Void visitBlock(BlockTree node, Scope scope) {
            scope = extend(node, scope, Scope.Kind.BLOCK);
            return super.visitBlock(node, scope);
        }

        @Override
        public Void visitLambda(LambdaTree node, Scope scope) {
            scope = extend(node, scope, Scope.Kind.LAMBDA);
            return super.visitLambda(node, scope);
        }


        private static void putIfAbsent(Tree caller, Scope scope, Symbol symbol) {
            Scope current = scope;
            do {
                if (current.content.containsKey(symbol.name)) {
                    throw Errors.alreadyDefinedError(symbol.name, caller.getLocation());
                }
                current = current.parent;
            }
            while (current.kind == Scope.Kind.BLOCK || current.kind == Scope.Kind.GLOBAL);

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

        private static Scope extend(Object context, Scope parent, Scope.Kind childKind) {
            Scope childScope = new Scope(context, childKind, parent);
            parent.children.put(context, childScope);
            return childScope;
        }
    }

}
