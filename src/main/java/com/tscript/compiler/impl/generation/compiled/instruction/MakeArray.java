package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class MakeArray implements Instruction {

    public final int count;

    public MakeArray(int count) {
        this.count = count;
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeMakeArray(this);
    }
}
