package tscriptc.scope;

import tscriptc.tree.ClassTree;
import tscriptc.tree.DefinitionTree;
import tscriptc.tree.Modifier;

import java.util.Collection;
import java.util.Set;

public class GlobalScope extends Scope {

    private int globals = 0;

    @Override
    public void putIfAbsent(SymbolKind kind, String name, Set<Modifier> modifiers) {
        if (content.containsKey(name)) return;;
        super.putIfAbsent(kind, name, modifiers);
        globals++;
    }

    @Override
    void notifyVariableAdded(int currentLocalsAmount) {
        if (currentLocalsAmount > nextFreeAddress)
            nextFreeAddress = currentLocalsAmount;
    }

    public int getGlobals() {
        return globals;
    }

    @Override
    public <P, R> R accept(ScopeVisitor<P, R> visitor, P p) {
        return visitor.visitGlobalScope(this, p);
    }

}
