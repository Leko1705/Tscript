package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class Goto extends AddressedInstruction {

    public Goto(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeGoto(this);
    }
}
