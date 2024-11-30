package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class BranchIfFalse extends AddressedInstruction {

    public BranchIfFalse(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeBranchIfFalse(this);
    }
}
