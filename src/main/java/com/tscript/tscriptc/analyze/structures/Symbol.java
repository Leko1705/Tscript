package com.tscript.tscriptc.analyze.structures;

import com.tscript.tscriptc.tree.Tree;

public class Symbol {

    public Symbol(String name, Visibility visibility, Tree tree, Kind kind, boolean isStatic) {
        this.name = name;
        this.visibility = visibility;
        this.tree = tree;
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
    public final Kind kind;
    public final boolean isStatic;
}
