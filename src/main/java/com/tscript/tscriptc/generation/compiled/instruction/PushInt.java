package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class PushInt extends ValueLoadInstruction<Integer> {

    public PushInt(Integer value) {
        super(value);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writePushInt(this);
    }
}
