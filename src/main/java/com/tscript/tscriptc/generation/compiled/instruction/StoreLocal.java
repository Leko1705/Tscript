package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class StoreLocal extends AddressedInstruction {

    public StoreLocal(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {

    }
}
