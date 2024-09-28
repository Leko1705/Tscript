package com.tscript.compiler.impl.utils;

import java.util.HashMap;
import java.util.Map;

public abstract class Scope {


    public enum Kind {
        GLOBAL,
        LOCAL,
        FUNCTION,
        LAMBDA,
        CLASS,
        NAMESPACE
    }

    public final Kind kind;

    public final Scope owner;

    public final Map<String, Symbol> symbols = new HashMap<>();

    public Scope(Kind kind, Scope owner) {
        this.kind = kind;
        this.owner = owner;
    }

}
