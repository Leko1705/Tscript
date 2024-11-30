package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class NotEquals implements Instruction {

    @Override
    public void write(InstructionWriter writer) {
        writer.writeNotEquals(this);
    }
}
