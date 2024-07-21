package com.tscript.tscriptc.tools;

public class CompilerProvider {

    private CompilerProvider(){
    }

    public static Compiler getDefaultTscriptCompiler(){
        return new TscriptCompiler();
    }

}
