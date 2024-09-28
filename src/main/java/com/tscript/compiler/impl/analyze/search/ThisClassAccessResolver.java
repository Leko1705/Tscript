package com.tscript.compiler.impl.analyze.search;

import com.tscript.compiler.impl.analyze.scoping.*;
import com.tscript.compiler.impl.analyze.structures.Symbol;

public class ThisClassAccessResolver implements ScopeVisitor<String, Symbol> {

    @Override
    public Symbol visitBlock(BlockScope scope, String s) {
        return scope.getEnclosingScope().accept(this, s);
    }

    @Override
    public Symbol visitClass(ClassScope scope, String s) {
        return scope.getSymbol(s);
    }

    @Override
    public Symbol visitFunction(FunctionScope scope, String s) {
        return scope.getEnclosingScope().accept(this, s);
    }

    @Override
    public Symbol visitGlobal(GlobalScope scope, String s) {
        return null;
    }

    @Override
    public Symbol visitLambda(LambdaScope scope, String s) {
        return scope.getSymbol(s);
    }

    @Override
    public Symbol visitNamespace(NamespaceScope scope, String s) {
        return null;
    }

    @Override
    public Symbol visitExternal(ExternalScope scope, String s) {
        return scope.getSymbol(s);
    }

}
