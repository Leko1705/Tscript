package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class StoreSuper extends AddressedInstruction {

    public StoreSuper(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeStoreSuper(this);
    }
}
