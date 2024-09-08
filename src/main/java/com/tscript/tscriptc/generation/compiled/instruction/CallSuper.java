package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class CallSuper implements Instruction {

    public final int argc;

    public CallSuper(int argc) {
        this.argc = argc;
    }

    @Override
    public void write(InstructionWriter writer) {

    }
}
