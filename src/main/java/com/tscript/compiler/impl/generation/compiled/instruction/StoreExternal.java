package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class StoreExternal extends AddressedInstruction {

    public StoreExternal(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeStoreExternal(this);
    }
}
