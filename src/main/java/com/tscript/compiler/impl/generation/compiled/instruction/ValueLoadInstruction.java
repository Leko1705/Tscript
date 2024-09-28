package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public abstract class ValueLoadInstruction<V> implements Instruction {

    public final V value;

    public ValueLoadInstruction(V value) {
        this.value = value;
    }

    public abstract void write(InstructionWriter writer);
}
