package tscriptc.scope;

public interface ScopeVisitor<P, R> {
    R visitGlobalScope(GlobalScope globalScope, P p);
    R visitLocalScope(LocalScope localScope, P p);
    R visitFunctionScope(FunctionScope functionScope, P p);
    R visitLambdaScope(LambdaScope lambdaScope, P p);
    R visitClassScope(ClassScope classScope, P p);
}
