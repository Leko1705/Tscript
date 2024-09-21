package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class LoadLocal extends AddressedInstruction {

    public LoadLocal(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeLoadLocal(this);
    }
}
