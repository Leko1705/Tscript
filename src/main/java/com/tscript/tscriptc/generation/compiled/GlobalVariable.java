package com.tscript.tscriptc.generation.compiled;

public class GlobalVariable {

    public final String name;
    public final boolean isMutable;

    public GlobalVariable(String name, boolean isMutable) {
        this.name = name;
        this.isMutable = isMutable;
    }
}
