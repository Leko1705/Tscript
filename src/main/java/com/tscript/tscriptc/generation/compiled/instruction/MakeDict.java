package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class MakeDict implements Instruction {

    public final int count;

    public MakeDict(int count) {
        this.count = count;
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeMakeDict(this);
    }
}
