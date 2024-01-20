package tscriptc.scope;

public class FunctionScope extends Scope {
    private final String name;
    public FunctionScope(String name, Scope enclosingScope) {
        this.name = name;
        setEnclosingScope(enclosingScope);
    }
    public String getName() {
        return name;
    }

    @Override
    void notifyVariableAdded(int currentLocalsAmount) {
        if (currentLocalsAmount > nextFreeAddress)
            nextFreeAddress = currentLocalsAmount;
    }

    @Override
    public <P, R> R accept(ScopeVisitor<P, R> visitor, P p) {
        return visitor.visitFunctionScope(this, p);
    }
}
