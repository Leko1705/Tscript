package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class StoreInternal extends AddressedInstruction {

    public StoreInternal(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {

    }
}
