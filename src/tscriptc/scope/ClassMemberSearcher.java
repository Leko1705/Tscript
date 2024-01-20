package tscriptc.scope;

public class ClassMemberSearcher implements ScopeVisitor<Void, Symbol> {

    private final String name;

    public ClassMemberSearcher(String name){
        this.name = name;
    }

    @Override
    public Symbol visitGlobalScope(GlobalScope globalScope, Void unused) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Symbol visitLocalScope(LocalScope localScope, Void unused) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Symbol visitFunctionScope(FunctionScope functionScope, Void unused) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Symbol visitLambdaScope(LambdaScope lambdaScope, Void unused) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Symbol visitClassScope(ClassScope classScope, Void unused) {

        if (classScope.has(name))
            return classScope.get(name);

        ClassScope superClass = classScope.getSuperClass();
        return superClass != null ? superClass.accept(this, null) : null;
    }

}
