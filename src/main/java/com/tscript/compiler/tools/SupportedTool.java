package com.tscript.compiler.tools;

public enum SupportedTool {

    DEFAULT_TSCRIPT_COMPILER(TscriptCompiler.class),

    TSCRIPT_TRANSPILER(TscriptTranspiler.class),

    TSCRIPT_BC_INSPECTOR(TscriptBytecodeInspector.class);


    final Class<? extends Tool> clazz;

    SupportedTool(Class<? extends Tool> clazz) {
        this.clazz = clazz;
    }
}
