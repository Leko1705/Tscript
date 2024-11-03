package com.tscript.compiler.tools;

public enum SupportedTools {

    DEFAULT_TSCRIPT_COMPILER(TscriptCompiler.class),

    TSCRIPT_TRANSPILER(TscriptTranspiler.class),

    TSCRIPT_BC_INSPECTOR(TscriptBytecodeInspector.class);


    final Class<? extends Tool> clazz;

    SupportedTools(Class<? extends Tool> clazz) {
        this.clazz = clazz;
    }
}
