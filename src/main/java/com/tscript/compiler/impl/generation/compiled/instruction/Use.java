package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class Use extends AddressedInstruction {

    public Use(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeUse(this);
    }
}
