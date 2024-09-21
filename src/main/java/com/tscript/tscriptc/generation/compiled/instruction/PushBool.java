package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class PushBool extends ValueLoadInstruction<Boolean> {

    public PushBool(Boolean value) {
        super(value);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writePushBool(this);
    }
}
