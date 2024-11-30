package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;
import com.tscript.compiler.source.tree.Operation;

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
