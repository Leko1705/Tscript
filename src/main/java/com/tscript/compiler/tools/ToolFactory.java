package com.tscript.compiler.tools;

public class ToolFactory {

    private ToolFactory(){
    }

    public static Compiler createDefaultTscriptCompiler(){
        return (Compiler) loadTool(SupportedTools.DEFAULT_TSCRIPT_COMPILER);
    }

    public static Tool loadTool(SupportedTools tool){
        try {
            return tool.clazz.getConstructor().newInstance();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
