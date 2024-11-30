package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class CallSuper implements Instruction {

    public final int argc;

    public CallSuper(int argc) {
        this.argc = argc;
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeCallSuper(this);
    }
}
