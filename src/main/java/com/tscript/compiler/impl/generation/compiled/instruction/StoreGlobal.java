package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class StoreGlobal extends AddressedInstruction {

    public StoreGlobal(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeStoreGlobal(this);
    }
}
