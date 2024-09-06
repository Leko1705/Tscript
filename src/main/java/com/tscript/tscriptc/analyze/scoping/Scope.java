package com.tscript.tscriptc.analyze.scoping;

import com.tscript.tscriptc.analyze.structures.Symbol;


public interface Scope {

    Scope getGlobalScope();

    Scope getChildScope(Object key);

    Symbol getSymbol(String name);

    default boolean hasSymbol(String name) {
        return getSymbol(name) != null;
    }

    <P, R> R accept(ScopeVisitor<P, R> visitor, P param);

}
