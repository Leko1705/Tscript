package com.tscript.tscriptc.scope;

public class LocalScope extends Scope {

    public LocalScope(Scope enclosingScope){
        setEnclosingScope(enclosingScope);
    }

    @Override
    void notifyVariableAdded(int currentLocalsAmount) {
        enclosingScope.notifyVariableAdded(currentLocalsAmount);
    }

    @Override
    public <P, R> R accept(ScopeVisitor<P, R> visitor, P p) {
        return visitor.visitLocalScope(this, p);
    }
}
