package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public interface Instruction {

    void write(InstructionWriter writer);

}
