package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class StoreStatic extends AddressedInstruction {

    public StoreStatic(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeStoreStatic(this);
    }
}
