package com.tscript.tscriptc.tools;

public class ToolFactory {

    private ToolFactory(){
    }

    public static Compiler createDefaultTscriptCompiler(){
        return new TscriptCompiler();
    }

}
