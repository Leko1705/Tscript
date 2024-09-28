package com.tscript.compiler.impl.analyze.scoping;

import com.tscript.compiler.impl.analyze.structures.Symbol;
import com.tscript.compiler.impl.analyze.structures.Visibility;

public interface ExternalScope extends Scope {

    String getName();

    <P, R> R accept(ScopeVisitor<P, R> visitor, P param);

    @Override
    default Scope getGlobalScope() {
        throw new UnsupportedOperationException();
    }

    @Override
    default Scope getChildScope(Object key) {
        return null;
    }

    @Override
    default Symbol getSymbol(String name) {
        return new Symbol(name, Visibility.PUBLIC, null, this, Symbol.Kind.UNKNOWN, true);
    }
}
