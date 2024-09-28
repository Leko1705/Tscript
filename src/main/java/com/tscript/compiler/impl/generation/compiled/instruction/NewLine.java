package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class NewLine implements Instruction {

    public final int line;

    public NewLine(int line) {
        this.line = line;
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeNewLine(this);
    }
}
