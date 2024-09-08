package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class MakeArray implements Instruction {

    public final int count;

    public MakeArray(int count) {
        this.count = count;
    }

    @Override
    public void write(InstructionWriter writer) {

    }
}
