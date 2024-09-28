package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public abstract class AddressedInstruction implements Instruction {

    public int address;

    public AddressedInstruction(int address) {
        this.address = address;
        if (address < 0)
            throw new IllegalArgumentException("address < 0");
    }

    public abstract void write(InstructionWriter writer);
}
