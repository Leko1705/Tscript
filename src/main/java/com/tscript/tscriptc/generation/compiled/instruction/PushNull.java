package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class PushNull extends ValueLoadInstruction<Void> {

    public PushNull() {
        super(null);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writePushNull(this);
    }
}
