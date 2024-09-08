package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public abstract class ValueLoadInstruction<V> implements Instruction {

    public final V value;

    public ValueLoadInstruction(V value) {
        this.value = value;
    }

    public abstract void write(InstructionWriter writer);
}
