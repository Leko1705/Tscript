package com.tscript.tscriptc.tools;

public enum Language {

    T_SCRIPT(TscriptCompiler.class),

    ;Language(Class<? extends Compiler> clazz){
        this.clazz = clazz;
    }

    final Class<? extends Compiler> clazz;

}
