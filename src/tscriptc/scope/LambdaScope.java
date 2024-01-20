package tscriptc.scope;

public class LambdaScope extends Scope {

    public LambdaScope(Scope enclosingScope){
        setEnclosingScope(enclosingScope);
    }


    @Override
    void notifyVariableAdded(int currentLocalsAmount) {
        if (currentLocalsAmount > nextFreeAddress)
            nextFreeAddress = currentLocalsAmount;
    }

    @Override
    public <P, R> R accept(ScopeVisitor<P, R> visitor, P p) {
        return visitor.visitLambdaScope(this, p);
    }
}
