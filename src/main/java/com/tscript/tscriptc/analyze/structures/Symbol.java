package com.tscript.tscriptc.analyze.structures;

import com.tscript.tscriptc.analyze.scoping.Scope;
import com.tscript.tscriptc.tree.Tree;

public class Symbol {

    public Symbol(String name, Visibility visibility, Tree tree, Scope scope, Kind kind, boolean isStatic) {
        this.name = name;
        this.visibility = visibility;
        this.tree = tree;
        this.scope = scope;
        this.kind = kind;
        this.isStatic = isStatic;
    }

    public enum Kind {
        VARIABLE,
        CONSTANT,
        FUNCTION,
        CLASS,
        NAMESPACE,
        UNKNOWN
    }

    public final String name;
    public final Visibility visibility;
    public final Tree tree;
    public final Scope scope;
    public final Kind kind;
    public final boolean isStatic;
}