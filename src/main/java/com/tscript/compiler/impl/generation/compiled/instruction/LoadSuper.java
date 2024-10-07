package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class LoadSuper extends AddressedInstruction {

    public LoadSuper(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeLoadSuper(this);
    }
}
