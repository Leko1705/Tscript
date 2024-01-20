package tscriptc.scope;

import tscriptc.tree.Modifier;

import java.util.Objects;
import java.util.Set;

public class GlobalSearcher implements ScopeVisitor<Void, Symbol> {

    private final String name;
    private final Set<Modifier> modifiers;

    public GlobalSearcher(String name, Modifier... modifier) {
        this.name = Objects.requireNonNull(name);
        this.modifiers = Set.of(modifier);
    }

    @Override
    public Symbol visitGlobalScope(GlobalScope globalScope, Void unused) {
        return getSymbol(globalScope);
    }

    @Override
    public Symbol visitLocalScope(LocalScope localScope, Void unused) {
        return localScope.getEnclosingScope().accept(this, null);
    }

    @Override
    public Symbol visitFunctionScope(FunctionScope functionScope, Void unused) {
        return functionScope.getEnclosingScope().accept(this, null);
    }

    @Override
    public Symbol visitLambdaScope(LambdaScope lambdaScope, Void unused) {
        return lambdaScope.getEnclosingScope().accept(this, null);
    }

    @Override
    public Symbol visitClassScope(ClassScope classScope, Void unused) {
        return classScope.getEnclosingScope().accept(this, null);
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
