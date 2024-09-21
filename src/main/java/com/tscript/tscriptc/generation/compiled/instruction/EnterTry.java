package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class EnterTry extends AddressedInstruction {

    public EnterTry(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeEnterTry(this);
    }
}
