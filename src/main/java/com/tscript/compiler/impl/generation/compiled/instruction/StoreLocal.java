package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class StoreLocal extends AddressedInstruction {

    public StoreLocal(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeStoreLocal(this);
    }
}
