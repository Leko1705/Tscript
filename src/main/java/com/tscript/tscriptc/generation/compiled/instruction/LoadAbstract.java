package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class LoadAbstract extends AddressedInstruction {

    public LoadAbstract(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeLoadAbstract(this);
    }
}
