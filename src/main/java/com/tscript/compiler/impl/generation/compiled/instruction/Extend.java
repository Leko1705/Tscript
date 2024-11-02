package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class Extend extends AddressedInstruction {

    public Extend(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writePutClosure(this);
    }
}
