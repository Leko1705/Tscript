package com.tscript.runtime.jit.graph;

import com.tscript.runtime.jit.graph.expr.Expression;
import com.tscript.runtime.jit.graph.traversal.TraversingStrategy;

public interface Node {

    Expression getExpr();

    void setNext(Node next);

    Node getNext();

    void step(TraversingStrategy walker, Context context);
}
