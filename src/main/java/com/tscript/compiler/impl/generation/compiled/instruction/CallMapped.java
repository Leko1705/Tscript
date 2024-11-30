package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class CallMapped implements Instruction {

    public final int argc;

    public CallMapped(int argc) {
        this.argc = argc;
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeCallMapped(this);
    }
}
