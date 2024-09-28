package com.tscript.compiler.impl.analyze.scoping;

import com.tscript.compiler.impl.analyze.structures.Symbol;


public interface Scope {

    Scope getGlobalScope();

    Scope getChildScope(Object key);

    Symbol getSymbol(String name);

    default boolean hasSymbol(String name) {
        return getSymbol(name) != null;
    }

    <P, R> R accept(ScopeVisitor<P, R> visitor, P param);

}
