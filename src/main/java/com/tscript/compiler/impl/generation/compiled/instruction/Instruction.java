package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public interface Instruction {

    void write(InstructionWriter writer);

}
