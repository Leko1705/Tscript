package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class Goto extends AddressedInstruction {

    public Goto(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeGoto(this);
    }
}
