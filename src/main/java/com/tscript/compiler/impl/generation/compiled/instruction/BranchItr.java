package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class BranchItr extends AddressedInstruction {

    public BranchItr(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeBranchItr(this);
    }
}
