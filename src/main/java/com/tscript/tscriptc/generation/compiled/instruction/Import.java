package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class Import extends AddressedInstruction {

    public Import(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {

    }
}
