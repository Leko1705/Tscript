package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class ItrNext implements Instruction {

    @Override
    public void write(InstructionWriter writer) {
        writer.writeItrNext(this);
    }

}
