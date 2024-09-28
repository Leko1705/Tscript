package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class CallInplace implements Instruction {

    public final int argc;

    public CallInplace(int argc) {
        this.argc = argc;
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeCallInplace(this);
    }
}
