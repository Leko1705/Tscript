package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class GetItr implements Instruction {

    @Override
    public void write(InstructionWriter writer) {
        writer.writeGetItr(this);
    }
}
