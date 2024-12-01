package com.tscript.runtime.jit.graph.expr;

public interface ExpressionVisitor<P, R> {

    R visitNull(Null expr, P p);

    R visitInt(Int expr, P p);

    R visitString(Str expr, P p);

    R visitBool(Bool expr, P p);

    R visitReal(Real expr, P p);
}
