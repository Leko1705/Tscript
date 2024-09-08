package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public abstract class AddressedInstruction implements Instruction {

    public int address;

    public AddressedInstruction(int address) {
        this.address = address;
    }

    public abstract void write(InstructionWriter writer);
}
