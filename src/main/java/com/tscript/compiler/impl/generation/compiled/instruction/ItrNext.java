package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class ItrNext implements Instruction {

    @Override
    public void write(InstructionWriter writer) {
        writer.writeItrNext(this);
    }

}