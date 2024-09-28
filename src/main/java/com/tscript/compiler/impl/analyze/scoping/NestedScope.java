package com.tscript.compiler.impl.analyze.scoping;

import com.tscript.compiler.impl.analyze.structures.Symbol;

public interface NestedScope extends Scope {

    Symbol getSymbol(String name);

    Scope getEnclosingScope();

    <P, R> R accept(ScopeVisitor<P, R> visitor, P param);
}
