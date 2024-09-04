package com.tscript.tscriptc.analyze.structures;

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

}
