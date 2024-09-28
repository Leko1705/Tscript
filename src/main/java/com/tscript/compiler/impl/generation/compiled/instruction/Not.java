package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class Not implements Instruction {

    @Override
    public void write(InstructionWriter writer) {
        writer.writeNot(this);
    }
}
