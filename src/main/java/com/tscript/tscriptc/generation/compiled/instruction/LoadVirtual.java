package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class LoadVirtual extends AddressedInstruction {

    public LoadVirtual(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {

    }
}
