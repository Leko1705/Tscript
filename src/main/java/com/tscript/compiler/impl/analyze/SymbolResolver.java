package com.tscript.compiler.impl.analyze;

import com.tscript.compiler.impl.utils.*;
import com.tscript.compiler.impl.utils.TCTree.*;
import com.tscript.compiler.impl.utils.Scope.*;
import com.tscript.compiler.impl.utils.Symbol.*;
import com.tscript.compiler.source.tree.DefinitionTree;
import com.tscript.compiler.source.tree.Modifier;

import java.util.HashSet;
import java.util.Set;

public class SymbolResolver {

    public static void resolve(TCTree tree){
        tree.accept(new ResolveVisitor());
    }


    private static class ResolveVisitor extends TCTreeScanner<Scope, Void> {

        private Set<Modifier> modifiers;

        private int nextAddress = 0;

        private int staticAddress = 0;

        private int classIndex = 0;

        @Override
        public Void visitRoot(TCRootTree node, Scope unused) {
            node.scope = new GlobalScope();
            return super.visitRoot(node, node.scope);
        }

        @Override
        public Void visitBlock(TCBlockTree node, Scope scope) {
            node.scope = new LocalScope(scope);
            int prevNextAddress = nextAddress;
            if (scope.kind == Scope.Kind.GLOBAL) nextAddress = 0;
            super.visitBlock(node, node.scope);
            nextAddress = prevNextAddress;
            return null;
        }

        @Override
        public Void visitFunction(TCFunctionTree node, Scope scope) {
            Set<Modifier> modifiers = getModifiers(node, scope);
            node.sym = new FunctionSymbol(node.name, modifiers, scope, index(modifiers), node.location);
            putIfAbsent(node, scope, node.sym);
            int prevNextAddress = nextAddress;
            nextAddress = 0;
            super.visitFunction(node, node.sym.subScope);
            nextAddress = prevNextAddress;
            return null;
        }

        @Override
        public Void visitParameter(TCParameterTree node, Scope scope) {
            scan(node.defaultValue, scope);
            node.sym = new VarSymbol(node.name, getModifiers(node, scope), scope, nextAddress++, node.location);
            putIfAbsent(node, scope, node.sym);
            return null;
        }

        @Override
        public Void visitNamespace(TCNamespaceTree node, Scope scope) {
            // since namespaces are compiled
            // as classes we give it an address
            Set<Modifier> modifiers = getModifiers(node, scope);
            node.sym = new ClassSymbol(node.name, modifiers, scope, index(modifiers), classIndex++, true, node.location);
            putIfAbsent(node, scope, node.sym);
            int prevNextAddress = nextAddress;
            int prevStaticAddress = staticAddress;
            nextAddress = 0;
            staticAddress = 0;
            super.visitNamespace(node, node.sym.subScope);
            nextAddress = prevNextAddress;
            staticAddress = prevStaticAddress;
            return null;
        }

        @Override
        public Void visitClass(TCClassTree node, Scope scope) {
            Set<Modifier> modifiers = getModifiers(node, scope);
            node.sym = new ClassSymbol(node.name, modifiers, scope, index(modifiers), classIndex++, false, node.location);
            putIfAbsent(node, scope, node.sym);
            int prevNextAddress = nextAddress;
            int prevStaticAddress = staticAddress;
            nextAddress = 0;
            staticAddress = 0;
            super.visitClass(node, node.sym.subScope);
            nextAddress = prevNextAddress;
            staticAddress = prevStaticAddress;
            return null;
        }

        @Override
        public Void visitConstructor(TCConstructorTree node, Scope scope) {
            node.scope = new FunctionScope(scope);
            int prevNextAddress = nextAddress;
            nextAddress = 0;
            super.visitConstructor(node, node.scope);
            nextAddress = prevNextAddress;
            return null;
        }

        @Override
        public Void visitLambda(TCLambdaTree node, Scope scope) {
            node.scope = new LambdaScope(scope);
            for (TCClosureTree cls : node.closures){
                scan(cls.initializer, scope);
                // address is -1 since closures are
                // compiled to private fields of the lambda
                cls.sym = new VarSymbol(cls.name, Set.of(), node.scope, Symbol.NO_ADDRESS, node.location);
                putIfAbsent(node, node.scope, cls.sym);
            }
            int prevNextAddress = nextAddress;
            nextAddress = 0;
            scan(node.parameters, node.scope);
            scan(node.body, node.scope);
            nextAddress = prevNextAddress;
            return null;
        }

        @Override
        public Void visitTryCatch(TCTryCatchTree node, Scope scope) {
            node.tryScope = new LocalScope(scope);

            int prevNextAddress = nextAddress;
            if (scope.kind == Scope.Kind.GLOBAL) nextAddress = 0;
            scan(node.tryStatement, node.tryScope);

            nextAddress = prevNextAddress;
            if (scope.kind == Scope.Kind.GLOBAL) nextAddress = 0;
            LocalScope catchScope = new LocalScope(scope);
            modifiers = Set.of();
            scan(node.exceptionVar, catchScope);
            scan(node.catchStatement, catchScope);
            nextAddress = prevNextAddress;

            return null;
        }

        @Override
        public Void visitIfElse(TCIfElseTree node, Scope scope) {
            node.thenScope = new LocalScope(scope);
            node.elseScope = new LocalScope(scope);
            int prevNextAddress = nextAddress;
            if (scope.kind == Scope.Kind.GLOBAL) nextAddress = 0;
            scan(node.thenStatement, node.thenScope);
            nextAddress = prevNextAddress;
            if (scope.kind == Scope.Kind.GLOBAL) nextAddress = 0;
            scan(node.elseStatement, node.elseScope);
            nextAddress = prevNextAddress;
            return null;
        }

        @Override
        public Void visitWhileDoLoop(TCWhileDoTree node, Scope scope) {
            node.scope = new LocalScope(scope);
            int prevNextAddress = nextAddress;
            if (scope.kind == Scope.Kind.GLOBAL) nextAddress = 0;
            super.visitWhileDoLoop(node, node.scope);
            nextAddress = prevNextAddress;
            return null;
        }

        @Override
        public Void visitDoWhileLoop(TCDoWhileTree node, Scope scope) {
            node.scope = new LocalScope(scope);
            int prevNextAddress = nextAddress;
            if (scope.kind == Scope.Kind.GLOBAL) nextAddress = 0;
            super.visitDoWhileLoop(node, node.scope);
            nextAddress = prevNextAddress;
            return null;
        }

        @Override
        public Void visitForLoop(TCForLoopTree node, Scope scope) {
            node.scope = new LocalScope(scope);
            int prevNextAddress = nextAddress;
            if (scope.kind == Scope.Kind.GLOBAL) nextAddress = 0;
            super.visitForLoop(node, node.scope);
            nextAddress = prevNextAddress;
            return null;
        }

        @Override
        public Void visitVarDefs(TCVarDefsTree node, Scope scope) {
            modifiers = node.modifiers.flags;
            return super.visitVarDefs(node, scope);
        }

        @Override
        public Void visitVarDef(TCVarDefTree node, Scope scope) {
            scan(node.initializer, scope);
            node.sym = new VarSymbol(node.name, modifiers, scope, index(modifiers), node.location);
            putIfAbsent(node, scope, node.sym);
            return null;
        }

        @Override
        public Void visitImport(TCImportTree node, Scope scope) {
            String name = node.accessChain.get(node.accessChain.size()-1);
            node.sym = new ImportedSymbol(name, nextAddress++, node.location);
            putIfAbsent(node, scope, node.sym);
            return null;
        }

        @Override
        public Void visitFromImport(TCFromImportTree node, Scope scope) {
            String name = node.importChain.get(node.importChain.size()-1);
            node.sym = new ImportedSymbol(name, nextAddress++, node.location);
            putIfAbsent(node, scope, node.sym);
            return super.visitFromImport(node, scope);
        }

        @Override
        public Void visitVariable(TCVariableTree node, Scope scope) {
            // do nothing. symbol type is determined while use-checking.
            return null;
        }


        private int index(Set<Modifier> modifiers){
            if (modifiers.contains(Modifier.STATIC))
                return staticAddress++;
            else
                return nextAddress++;
        }

    }


    private static void putIfAbsent(TCTree caller, Scope scope, Symbol symbol) {
        Symbol prevDecl = scope.accept(new AlreadyDeclared(symbol.name));
        if (prevDecl != null)
            throw Errors.alreadyDefinedError(symbol.name, caller.location);
        scope.symbols.put(symbol.name, symbol);
    }


    private record AlreadyDeclared(String name) implements Scope.Visitor<Symbol> {

        @Override
        public Symbol visitGlobal(GlobalScope scope) {
            return scope.symbols.get(name);
        }

        @Override
        public Symbol visitLocal(LocalScope scope) {
            Symbol sym = scope.symbols.get(name);
            if (sym != null)
                return sym;
            return scope.enclosing.accept(this);
        }

        @Override
        public Symbol visitFunction(FunctionScope scope) {
            return scope.symbols.get(name);
        }

        @Override
        public Symbol visitLambda(LambdaScope scope) {
            return scope.symbols.get(name);
        }

        @Override
        public Symbol visitClass(ClassScope scope) {
            return scope.symbols.get(name);
        }


    }

    private static Set<Modifier> getModifiers(TCDefinitionTree defTree, Scope currScope){
        Set<Modifier> modifiers = new HashSet<>(defTree.modifiers.flags);

        if (currScope.owner != null && currScope.owner.sym.isNamespace) {
            boolean hasVisibility = false;
            for (Modifier modifier : modifiers) {
                if (modifier.isVisibility()) {
                    hasVisibility = true;
                    break;
                }
            }
            if (!hasVisibility)
                modifiers.add(Modifier.PUBLIC);
            modifiers.add(Modifier.STATIC);

        }

        return modifiers;
    }

}
