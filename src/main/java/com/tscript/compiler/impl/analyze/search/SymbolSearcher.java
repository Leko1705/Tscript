package com.tscript.compiler.impl.analyze.search;

import com.tscript.compiler.impl.analyze.scoping.*;
import com.tscript.compiler.impl.analyze.structures.Symbol;

public class SymbolSearcher implements ScopeVisitor<String, Symbol> {

    @Override
    public Symbol visitBlock(BlockScope scope, String s) {
        Symbol sym = scope.getSymbol(s);
        if (sym != null) return sym;
        return scope.getEnclosingScope().accept(this, s);
    }

    @Override
    public Symbol visitClass(ClassScope scope, String s) {
        return scope.getGlobalScope().accept(this, s);
    }

    @Override
    public Symbol visitFunction(FunctionScope scope, String s) {
        Symbol sym = scope.getSymbol(s);
        if (sym != null) return sym;
        return scope.getEnclosingScope().accept(this, s);
    }

    @Override
    public Symbol visitGlobal(GlobalScope scope, String s) {
        return scope.getSymbol(s);
    }

    @Override
    public Symbol visitLambda(LambdaScope scope, String s) {
        Symbol sym = scope.getSymbol(s);
        if (sym != null) return sym;
        return scope.getGlobalScope().accept(this, s);
    }

    @Override
    public Symbol visitNamespace(NamespaceScope scope, String s) {
        return scope.getSymbol(s);
    }

    @Override
    public Symbol visitExternal(ExternalScope scope, String s) {
        return scope.getSymbol(s);
    }

}