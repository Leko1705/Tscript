package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class LoadName extends AddressedInstruction {

    public LoadName(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeLoadName(this);
    }
}
