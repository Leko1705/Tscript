package com.tscript.compiler.impl.analyze;

import com.tscript.compiler.impl.utils.*;
import com.tscript.compiler.source.tree.Modifier;
import com.tscript.compiler.source.tree.ThisTree;
import com.tscript.runtime.core.Builtins;

public class UsageApplier {

    public static void apply(TCTree tree){
        tree.accept(new Checker());
    }

    private static class Checker extends TCTreeScanner<Scope, Void> {

        private boolean inStaticContext = false;
        private boolean inSuperConstructorParams = false;
        
        private boolean localUsePerformed = false;
        private boolean globalUsePerformed = false;
        private boolean inGlobalScope;

        @Override
        public Void visitRoot(TCTree.TCRootTree node, Scope unused) {
            inGlobalScope = false; // avoid any miss-behaviour
            scan(node.imports, node.scope);
            scan(node.definitions, node.scope);
            inGlobalScope = true;
            scan(node.statements, node.scope);
            return null;
        }

        @Override
        public Void visitClass(TCTree.TCClassTree node, Scope scope) {
            boolean prevInGlobalScope = inGlobalScope;
            inGlobalScope = false;
            boolean prevLocalUsePerformed = localUsePerformed;
            super.visitClass(node, node.sym.subScope);
            inGlobalScope = prevInGlobalScope;
            localUsePerformed = prevLocalUsePerformed;
            return null;
        }

        @Override
        public Void visitFunction(TCTree.TCFunctionTree node, Scope unused) {
            boolean prev = inStaticContext;
            inStaticContext = node.modifiers.flags.contains(Modifier.STATIC);
            boolean prevInGlobalScope = inGlobalScope;
            inGlobalScope = false;
            boolean prevLocalUsePerformed = localUsePerformed;
            super.visitFunction(node, node.sym.subScope);
            inStaticContext = prev;
            inGlobalScope = prevInGlobalScope;
            localUsePerformed = prevLocalUsePerformed;
            return null;
        }

        @Override
        public Void visitConstructor(TCTree.TCConstructorTree node, Scope scope) {
            scan(node.modifiers, node.scope);
            scan(node.parameters, node.scope);
            boolean prevStat = inStaticContext;
            inStaticContext = false;
            inSuperConstructorParams = true;
            boolean prevLocalUsePerformed = localUsePerformed;
            scan(node.superArgs, node.scope);
            inStaticContext = prevStat;
            inSuperConstructorParams = false;
            scan(node.body, scope);
            localUsePerformed = prevLocalUsePerformed;
            return null;
        }

        @Override
        public Void visitLambda(TCTree.TCLambdaTree node, Scope unused) {
            boolean prevInGlobalScope = inGlobalScope;
            inGlobalScope = false;
            boolean prevLocalUsePerformed = localUsePerformed;
            super.visitLambda(node, node.scope);
            inGlobalScope = prevInGlobalScope;
            localUsePerformed = prevLocalUsePerformed;
            return null;
        }

        @Override
        public Void visitNamespace(TCTree.TCNamespaceTree node, Scope unused) {
            boolean prevInGlobalScope = inGlobalScope;
            inGlobalScope = false;
            boolean prevLocalUsePerformed = localUsePerformed;
            super.visitNamespace(node, node.sym.subScope);
            inGlobalScope = prevInGlobalScope;
            localUsePerformed = prevLocalUsePerformed;
            return null;
        }

        @Override
        public Void visitBlock(TCTree.TCBlockTree node, Scope unused) {
            boolean prevInGlobalScope = inGlobalScope;
            inGlobalScope = false;
            boolean prevLocalUsePerformed = localUsePerformed;
            super.visitBlock(node, node.scope);
            inGlobalScope = prevInGlobalScope;
            localUsePerformed = prevLocalUsePerformed;
            return null;
        }

        @Override
        public Void visitIfElse(TCTree.TCIfElseTree node, Scope scope) {
            scan(node.condition, scope);
            boolean prevInGlobalScope = inGlobalScope;
            inGlobalScope = false;
            boolean prevLocalUsePerformed = localUsePerformed;
            scan(node.thenStatement, node.thenScope);
            scan(node.elseStatement, node.elseScope);
            inGlobalScope = prevInGlobalScope;
            localUsePerformed = prevLocalUsePerformed;
            return null;
        }

        @Override
        public Void visitWhileDoLoop(TCTree.TCWhileDoTree node, Scope scope) {
            boolean prevInGlobalScope = inGlobalScope;
            inGlobalScope = false;
            boolean prevLocalUsePerformed = localUsePerformed;
            scan(node.condition, scope);
            scan(node.statement, node.scope);
            inGlobalScope = prevInGlobalScope;
            localUsePerformed = prevLocalUsePerformed;
            return null;
        }

        @Override
        public Void visitDoWhileLoop(TCTree.TCDoWhileTree node, Scope scope) {
            boolean prevInGlobalScope = inGlobalScope;
            inGlobalScope = false;
            boolean prevLocalUsePerformed = localUsePerformed;
            scan(node.statement, node.scope);
            scan(node.condition, scope);
            inGlobalScope = prevInGlobalScope;
            localUsePerformed = prevLocalUsePerformed;
            return null;
        }

        @Override
        public Void visitForLoop(TCTree.TCForLoopTree node, Scope scope) {
            boolean prevInGlobalScope = inGlobalScope;
            inGlobalScope = false;
            boolean prevLocalUsePerformed = localUsePerformed;
            super.visitForLoop(node, node.scope);
            inGlobalScope = prevInGlobalScope;
            localUsePerformed = prevLocalUsePerformed;
            return null;
        }

        @Override
        public Void visitTryCatch(TCTree.TCTryCatchTree node, Scope scope) {
            boolean prevInGlobalScope = inGlobalScope;
            inGlobalScope = false;
            boolean prevLocalUsePerformed = localUsePerformed;
            scan(node.tryStatement, node.tryScope);
            localUsePerformed = prevLocalUsePerformed;
            scan(node.exceptionVar, node.exceptionVar.sym.owner);
            scan(node.catchStatement, node.exceptionVar.sym.owner);
            inGlobalScope = prevInGlobalScope;
            localUsePerformed = prevLocalUsePerformed;
            return null;
        }

        @Override
        public Void visitVariable(TCTree.TCVariableTree node, Scope scope) {
            
            if (globalUsePerformed || localUsePerformed){
                node.sym = new Symbol.UnknownSymbol(node.name, node.location);
                return null;
            }

            boolean hasSuperClass = hasSuperClass(scope);
            boolean superClassIsImported = hasSuperClass && superClassIsImported(scope);

            Symbol sym = scope.accept(new SimpleSymbolResolver(node.getName()));
            if (sym == null && hasSuperClass && !superClassIsImported){
                if (scope.owner.sym.superClass.kind == Symbol.Kind.CLASS){
                    Symbol.ClassSymbol superSym = (Symbol.ClassSymbol) scope.owner.sym.superClass;
                    sym = superSym.subScope.accept(new SuperSymbolResolver(node.getName()));
                    if (sym != null){
                        if (sym.modifiers.contains(Modifier.PRIVATE))
                            throw Errors.memberIsNotVisible(node.location, Modifier.PRIVATE, node.name);
                        if (inSuperConstructorParams)
                            throw Errors.canNotUseBeforeConstructorCalled(node.location, node.name);
                        sym = sym.clone();
                        sym.inSuperClass = true;
                    }
                }

            }

            if (sym == null && hasSuperClass && superClassIsImported) {
                node.sym = new Symbol.UnknownSymbol(node.name, node.location);
                return null;
            }

            if (scope.owner != null && scope.owner.sym.name.equals(node.name)){
                node.sym = scope.owner.sym;
                return null;
            }

            if (sym == null) {
                if (Builtins.indexOf(node.name) != -1){
                    node.sym = new Symbol.Builtin(node.name, node.location);
                    return null;
                }
                else {
                    throw Errors.canNotFindSymbol(node.getName(), node.location);
                }
            }

            node.sym = sym;

            if (sym.owner == scope.enclosing){
                // in current class
                if (inSuperConstructorParams)
                    throw Errors.canNotUseBeforeConstructorCalled(node.location, node.name);
            }
            if (inStaticContext
                    && sym.owner.kind == Scope.Kind.CLASS
                    && !sym.modifiers.contains(Modifier.STATIC)){
                throw Errors.canNotAccessFromStaticContext(node.location);
            }

            return null;
        }

        @Override
        public Void visitSuper(TCTree.TCSuperTree node, Scope scope) {
            if (inSuperConstructorParams)
                throw Errors.canNotUseBeforeConstructorCalled(node.location, "super");

            Scope.ClassScope currClass = scope.owner;

            if (!hasSuperClass(scope)){
                throw Errors.invalidSuperAccessFound(node.location, currClass.sym.name);
            }

            if (superClassIsImported(scope))
                return null;

            // has at least a super class
            Symbol sym = null;
            if (currClass.sym.superClass.kind == Symbol.Kind.CLASS){
                Symbol.ClassSymbol superSym = (Symbol.ClassSymbol) currClass.sym.superClass;
                sym = superSym.subScope.accept(new SuperSymbolResolver(node.getName()));
            }

            if (sym != null){
                if (sym.modifiers.contains(Modifier.PRIVATE)){
                    throw Errors.memberIsNotVisible(node.location, Modifier.PRIVATE, node.name);
                }
                sym = sym.clone();
                sym.inSuperClass = true;
            }
            else {
                throw Errors.noSuchMemberFound(node.location, currClass.sym.superClass.name, node.name);
            }

            return null;
        }

        private static boolean hasSuperClass(Scope scope){
            if (scope.owner == null || scope.owner.kind != Scope.Kind.CLASS)
                return false;
            Scope.ClassScope clsScope = scope.owner;
            return clsScope.sym.superClass != null;
        }

        private static boolean superClassIsImported(Scope scope){
            while (scope.owner.sym.superClass != null){
                if (scope.owner.sym.superClass.kind == Symbol.Kind.IMPORTED)
                    return true;
                scope = ((Symbol.ClassSymbol)scope.owner.sym.superClass).subScope;
            }
            return false;
        }

        @Override
        public Void visitMemberAccess(TCTree.TCMemberAccessTree node, Scope scope) {
            super.visitMemberAccess(node, scope);
            if (node.expression instanceof ThisTree){
                Scope.ClassScope clsScope = scope.owner;
                node.sym = clsScope.symbols.get(node.memberName);
                if (node.sym == null)
                    throw Errors.noSuchMemberFound(node.location, clsScope.sym.name, node.memberName);
            }
            return null;
        }

        @Override
        public Void visitThis(TCTree.TCThisTree node, Scope scope) {
            if (inSuperConstructorParams)
                throw Errors.canNotUseBeforeConstructorCalled(node.location, "this");
            return null;
        }

        @Override
        public Void visitUse(TCTree.TCUseTree node, Scope scope) {
            super.visitUse(node, scope);
            localUsePerformed = true;
            if (inGlobalScope)
                globalUsePerformed = true;
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
            if (scope.sym.modifiers.contains(Modifier.STATIC)) {
                if (scope.enclosing.kind == Scope.Kind.CLASS){
                    Scope.ClassScope encl = (Scope.ClassScope) scope.enclosing;
                    if (encl.sym.isNamespace) {
                        sym = scope.enclosing.accept(this);
                        if (sym != null) return sym;
                    }
                }
                return scope.topLevel.accept(this);
            }
            else
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
            if (scope.sym.superClass.kind == Symbol.Kind.CLASS){
                return ((Symbol.ClassSymbol)scope.sym.superClass).subScope.accept(this);
            }
            return null;
        }

    }

}
