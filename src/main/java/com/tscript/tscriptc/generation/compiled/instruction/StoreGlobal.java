package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class StoreGlobal extends AddressedInstruction {

    public StoreGlobal(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {

    }
}
