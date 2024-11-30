package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class Import extends AddressedInstruction {

    public Import(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeImport(this);
    }
}
