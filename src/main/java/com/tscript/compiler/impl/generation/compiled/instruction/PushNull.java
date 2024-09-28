package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class PushNull extends ValueLoadInstruction<Void> {

    public PushNull() {
        super(null);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writePushNull(this);
    }
}
