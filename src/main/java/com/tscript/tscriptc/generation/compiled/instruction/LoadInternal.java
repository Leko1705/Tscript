package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class LoadInternal extends AddressedInstruction {

    public LoadInternal(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {

    }
}
