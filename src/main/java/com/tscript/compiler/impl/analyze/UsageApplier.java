package com.tscript.compiler.impl.analyze;

import com.tscript.compiler.impl.utils.*;
import com.tscript.compiler.source.tree.Modifier;

public class UsageApplier {

    public static void apply(TCTree tree){
        tree.accept(new Checker());
    }

    private static class Checker extends TCTreeScanner<Scope, Void> {

        @Override
        public Void visitRoot(TCTree.TCRootTree node, Scope unused) {
            return super.visitRoot(node, node.scope);
        }

        @Override
        public Void visitClass(TCTree.TCClassTree node, Scope unused) {
            return super.visitClass(node, node.sym.subScope);
        }

        @Override
        public Void visitFunction(TCTree.TCFunctionTree node, Scope unused) {
            return super.visitFunction(node, node.sym.subScope);
        }

        @Override
        public Void visitLambda(TCTree.TCLambdaTree node, Scope unused) {
            return super.visitLambda(node, node.scope);
        }

        @Override
        public Void visitNamespace(TCTree.TCNamespaceTree node, Scope unused) {
            return super.visitNamespace(node, node.sym.subScope);
        }

        @Override
        public Void visitBlock(TCTree.TCBlockTree node, Scope unused) {
            return super.visitBlock(node, node.scope);
        }

        @Override
        public Void visitIfElse(TCTree.TCIfElseTree node, Scope scope) {
            scan(node.condition, scope);
            scan(node.thenStatement, node.thenScope);
            scan(node.elseStatement, node.elseScope);
            return null;
        }

        @Override
        public Void visitWhileDoLoop(TCTree.TCWhileDoTree node, Scope scope) {
            scan(node.condition, scope);
            scan(node.statement, node.scope);
            return null;
        }

        @Override
        public Void visitDoWhileLoop(TCTree.TCDoWhileTree node, Scope scope) {
            scan(node.statement, node.scope);
            scan(node.condition, scope);
            return null;
        }

        @Override
        public Void visitForLoop(TCTree.TCForLoopTree node, Scope scope) {
            return super.visitForLoop(node, node.scope);
        }

        @Override
        public Void visitTryCatch(TCTree.TCTryCatchTree node, Scope scope) {
            scan(node.tryStatement, node.tryScope);
            scan(node.exceptionVar, node.exceptionVar.sym.owner);
            scan(node.catchStatement, node.exceptionVar.sym.owner);
            return null;
        }

        @Override
        public Void visitVariable(TCTree.TCVariableTree node, Scope scope) {
            Symbol sym = scope.accept(new SimpleSymbolResolver(node.getName()));
            if (sym == null && scope.owner != null && scope.owner.kind == Scope.Kind.CLASS){
                Scope.ClassScope clsScope = (Scope.ClassScope) scope;
                if (clsScope.sym.superClass != null){
                    sym = clsScope.sym.superClass.subScope.accept(new SuperSymbolResolver(node.getName()));
                }
            }
            if (sym != null){
                node.sym = sym;
            }
            else {
                throw Errors.canNotFindSymbol(node.getName(), node.location);
            }
            return null;
        }
    }


    private record SimpleSymbolResolver(String name) implements Scope.Visitor<Symbol> {

        @Override
        public Symbol visitGlobal(Scope.GlobalScope scope) {
            return scope.symbols.get(name);
        }

        @Override
        public Symbol visitLocal(Scope.LocalScope scope) {
            Symbol sym = scope.symbols.get(name);
            if (sym != null) return sym;
            return scope.enclosing.accept(this);
        }

        @Override
        public Symbol visitFunction(Scope.FunctionScope scope) {
            Symbol sym = scope.symbols.get(name);
            if (sym != null) return sym;
            return scope.enclosing.accept(this);
        }

        @Override
        public Symbol visitLambda(Scope.LambdaScope scope) {
            Symbol sym = scope.symbols.get(name);
            if (sym != null) return sym;
            return scope.topLevel.accept(this);
        }

        @Override
        public Symbol visitClass(Scope.ClassScope scope) {
            Symbol sym = scope.symbols.get(name);
            if (sym != null) return sym;
            if (scope.sym.superClass != null) {
                sym = scope.sym.superClass.subScope.accept(this);
                if (sym != null) return sym;
            }
            if (scope.sym.modifiers.contains(Modifier.STATIC))
                return scope.topLevel.accept(this);
            else
                return scope.enclosing.accept(this);
        }

        @Override
        public Symbol visitNamespace(Scope.NamespaceScope scope) {
            Symbol sym = scope.symbols.get(name);
            if (sym != null) return sym;
            return scope.enclosing.accept(this);
        }
    }


    private record SuperSymbolResolver(String name) implements Scope.Visitor<Symbol> {

        @Override
        public Symbol visitGlobal(Scope.GlobalScope scope) {
            return null;
        }

        @Override
        public Symbol visitLocal(Scope.LocalScope scope) {
            if (scope.owner == null) return null;
            return scope.owner.accept(this);
        }

        @Override
        public Symbol visitFunction(Scope.FunctionScope scope) {
            if (scope.owner == null) return null;
            return scope.owner.accept(this);
        }

        @Override
        public Symbol visitLambda(Scope.LambdaScope scope) {
            return null;
        }

        @Override
        public Symbol visitClass(Scope.ClassScope scope) {
            Symbol sym = scope.symbols.get(name);
            if (sym != null) return sym;
            if (scope.sym.superClass == null) return null;
            return scope.sym.superClass.subScope.accept(this);
        }

        @Override
        public Symbol visitNamespace(Scope.NamespaceScope scope) {
            return null;
        }
    }

}
