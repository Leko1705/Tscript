package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class EnterTry extends AddressedInstruction {

    public EnterTry(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeEnterTry(this);
    }
}
