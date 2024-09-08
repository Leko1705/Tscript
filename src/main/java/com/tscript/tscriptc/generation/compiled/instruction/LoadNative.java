package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class LoadNative extends AddressedInstruction {

    public LoadNative(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {

    }

}
