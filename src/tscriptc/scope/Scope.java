package tscriptc.scope;

import tscriptc.tree.Modifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class Scope {
    Scope enclosingScope;
    final Map<String, Symbol> content = new HashMap<>();
    int nextFreeAddress = 0;

    public Symbol get(String name){
        return content.get(name);
    }
    public boolean has(String name){
        return content.containsKey(name);
    }
    public void putIfAbsent(SymbolKind kind, String name, Set<Modifier> modifiers){
        if (content.containsKey(name)) return;
        Symbol symbol = new Symbol(this, kind, name, nextFreeAddress++);
        symbol.getModifiers().addAll(modifiers);
        content.put(name, symbol);
        notifyVariableAdded(nextFreeAddress);
    }
    public Scope getEnclosingScope() {
        return enclosingScope;
    }
    public void setEnclosingScope(Scope enclosingScope) {
        this.enclosingScope = enclosingScope;
        if (!(enclosingScope instanceof GlobalScope
                || enclosingScope instanceof ClassScope))
            this.nextFreeAddress = enclosingScope.nextFreeAddress;
    }

    public int getLocals(){
        return nextFreeAddress;
    }

    abstract void notifyVariableAdded(int currentLocalsAmount);
    public abstract <P, R> R accept(ScopeVisitor<P, R> visitor, P p);

}
