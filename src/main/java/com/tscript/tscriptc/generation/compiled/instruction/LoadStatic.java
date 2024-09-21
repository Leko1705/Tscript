package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class LoadStatic extends AddressedInstruction {

    public LoadStatic(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeLoadStatic(this);
    }
}
