package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class LoadType extends AddressedInstruction {

    public LoadType(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {

    }
}
