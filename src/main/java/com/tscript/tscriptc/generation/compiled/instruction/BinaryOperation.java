package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;
import com.tscript.tscriptc.tree.Operation;

public class BinaryOperation implements Instruction {

    public final Operation operation;

    public BinaryOperation(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeBinaryOperation(this);
    }
}
