package com.tscript.tscriptc.analyze.search;

import com.tscript.tscriptc.analyze.scoping.*;
import com.tscript.tscriptc.analyze.structures.Symbol;

public class SuperClassAccessResolver implements ScopeVisitor<String, Symbol> {

    @Override
    public Symbol visitBlock(BlockScope scope, String s) {
        return scope.getEnclosingScope().accept(this, s);
    }

    @Override
    public Symbol visitClass(ClassScope scope, String s) {
        Scope curr = scope;
        while (curr != null){
            Symbol sym = curr.getSymbol(s);
            if (sym != null) return sym;

            if (curr instanceof ClassScope cls)
                curr = cls.getSuperClassScope();
            else
                curr = null;
        }
        return null;
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
        return null;
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
