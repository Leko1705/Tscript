package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class ToMapArg extends AddressedInstruction {

    public ToMapArg(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {

    }
}
