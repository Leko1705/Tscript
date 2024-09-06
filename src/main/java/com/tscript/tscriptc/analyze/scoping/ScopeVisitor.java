package com.tscript.tscriptc.analyze.scoping;

import jdk.jfr.Experimental;

@Experimental
public interface ScopeVisitor<P, R> {

    R visitBlock(BlockScope scope, P p);

    R visitClass(ClassScope scope, P p);

    R visitFunction(FunctionScope scope, P p);

    R visitGlobal(GlobalScope scope, P p);

    R visitLambda(LambdaScope scope, P p);

    R visitNamespace(NamespaceScope scope, P p);

    R visitExternal(ExternalScope scope, P p);

}
