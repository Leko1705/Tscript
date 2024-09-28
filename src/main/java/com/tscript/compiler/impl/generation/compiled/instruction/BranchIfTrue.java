package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class BranchIfTrue extends AddressedInstruction {

    public BranchIfTrue(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeBranchIfTrue(this);
    }
}
