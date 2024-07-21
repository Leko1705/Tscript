package com.tscript.tscriptc.tools;

import com.tscript.tscriptc.plugin.Plugin;

public class CompilerBuilder {

    public CompilerBuilder(Language language){

    }

    public CompilerBuilder setDisassembled(boolean disassembled){
        return this;
    }

    public CompilerBuilder addPlugin(Plugin plugin){
        return this;
    }

    public Compiler build(){
        return null;
    }

}
