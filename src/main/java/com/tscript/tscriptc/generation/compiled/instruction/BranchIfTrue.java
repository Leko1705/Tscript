package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class BranchIfTrue extends AddressedInstruction {

    public BranchIfTrue(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeBranchIfTrue(this);
    }
}
