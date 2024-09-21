package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class BranchIfFalse extends AddressedInstruction {

    public BranchIfFalse(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeBranchIfFalse(this);
    }
}
