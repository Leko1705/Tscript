package com.tscript.tscriptc.analyze.scoping;

import com.tscript.tscriptc.analyze.structures.Symbol;

public interface NestedScope extends Scope {

    Symbol getSymbol(String name);

    Scope getEnclosingScope();

    <P, R> R accept(ScopeVisitor<P, R> visitor, P param);
}
