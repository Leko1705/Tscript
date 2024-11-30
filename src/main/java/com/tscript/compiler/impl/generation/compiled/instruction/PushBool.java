package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class PushBool extends ValueLoadInstruction<Boolean> {

    public PushBool(Boolean value) {
        super(value);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writePushBool(this);
    }
}
