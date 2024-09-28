package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class ToMapArg extends AddressedInstruction {

    public ToMapArg(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeToMapArg(this);
    }
}
