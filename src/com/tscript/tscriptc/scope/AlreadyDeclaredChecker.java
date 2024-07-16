package com.tscript.tscriptc.scope;

public class AlreadyDeclaredChecker implements ScopeVisitor<String, Boolean> {
    
    @Override
    public Boolean visitGlobalScope(GlobalScope globalScope, String name) {
        return globalScope.has(name);
    }

    @Override
    public Boolean visitLocalScope(LocalScope localScope, String name) {
        if (localScope.has(name)) return true;
        return localScope.getEnclosingScope().accept(this, name);
    }

    @Override
    public Boolean visitFunctionScope(FunctionScope functionScope, String name) {
        return functionScope.has(name);
    }

    @Override
    public Boolean visitLambdaScope(LambdaScope lambdaScope, String name) {
        return lambdaScope.has(name);
    }

    @Override
    public Boolean visitClassScope(ClassScope classScope, String name) {
        return classScope.has(name);
    }
}
