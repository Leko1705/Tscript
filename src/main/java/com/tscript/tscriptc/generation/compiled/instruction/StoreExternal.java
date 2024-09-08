package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class StoreExternal extends AddressedInstruction {

    public StoreExternal(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {

    }
}
