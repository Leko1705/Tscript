package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class LoadGlobal extends AddressedInstruction {

    public LoadGlobal(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {

    }
}