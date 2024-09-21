package com.tscript.tscriptc.tools;

public class ToolFactory {

    private ToolFactory(){
    }

    public static Compiler createDefaultTscriptCompiler(){
        return (Compiler) loadTool(SupportedTool.DEFAULT_TSCRIPT_COMPILER);
    }

    public static Tool loadTool(SupportedTool tool){
        try {
            return tool.clazz.getConstructor().newInstance();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
