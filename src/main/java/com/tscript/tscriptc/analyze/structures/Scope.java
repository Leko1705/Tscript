package com.tscript.tscriptc.analyze.structures;

import com.tscript.tscriptc.tree.Tree;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    public enum Kind {
        GLOBAL,
        FUNCTION,
        CLASS,
        NAMESPACE,
        LAMBDA,
        BLOCK,
        CONSTRUCTOR
    }

    public final Object owner;
    public final Kind kind;
    public final Scope parent;
    public Scope topLevel;
    public final Map<String, Symbol> content;
    public final Map<Object, Scope> children;


    public Scope(Object owner, Kind kind, Scope parent) {
        this.owner = owner;
        this.kind = kind;
        this.parent = parent;
        this.content = new HashMap<>();
        this.children = new HashMap<>();

        if (parent == null)
            this.topLevel = this;
        else
            this.topLevel = parent.topLevel;
    }


}
