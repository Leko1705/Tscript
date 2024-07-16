package com.tscript.tscriptc.scope;

import com.tscript.tscriptc.tree.Modifier;

import java.util.Objects;
import java.util.Set;

public class SimpleSymbolSearcher implements ScopeVisitor<Void, Symbol> {

    private final String name;
    private final Set<Modifier> modifiers;

    private final GlobalSearcher globalSearcher;

    public SimpleSymbolSearcher(String name, Modifier... modifier) {
        this.name = Objects.requireNonNull(name);
        this.modifiers = Set.of(modifier);
        globalSearcher = new GlobalSearcher(name, modifier);
    }

    @Override
    public Symbol visitGlobalScope(GlobalScope globalScope, Void unused) {
        return getSymbol(globalScope);
    }

    @Override
    public Symbol visitLocalScope(LocalScope localScope, Void unused) {
        Symbol symbol = getSymbol(localScope);
        if (symbol != null) return symbol;
        return localScope.getEnclosingScope().accept(this, null);
    }

    @Override
    public Symbol visitFunctionScope(FunctionScope functionScope, Void unused) {
        Symbol symbol = getSymbol(functionScope);
        if (symbol != null) return symbol;
        return functionScope.getEnclosingScope().accept(this, null);
    }

    @Override
    public Symbol visitLambdaScope(LambdaScope lambdaScope, Void unused) {
        Symbol sym = lambdaScope.get(name);
        return sym != null ? sym : globalSearcher.visitLambdaScope(lambdaScope, null);
    }

    @Override
    public Symbol visitClassScope(ClassScope classScope, Void canEscapeClassScope) {

        Symbol symbol = classScope.get(name);
        if (symbol != null) return symbol;

        ClassScope superClass = classScope.getSuperClass();
        if (superClass != null){
            symbol = superClass.accept(this, canEscapeClassScope);
            if (symbol != null) return symbol;
        }

        Scope enclosingScope = classScope.getEnclosingScope();
        return enclosingScope.accept(this, null);
    }
    
    private Symbol getSymbol(Scope scope){
        Symbol symbol = scope.get(name);
        if (symbol == null || !hasRequiredModifiers(symbol))
            return null;
        return symbol;
    }
    
    private boolean hasRequiredModifiers(Symbol symbol){
        return symbol.getModifiers().containsAll(modifiers);
    }

}
