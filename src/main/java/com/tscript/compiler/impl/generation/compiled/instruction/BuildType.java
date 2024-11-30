package com.tscript.compiler.impl.generation.compiled.instruction;

import com.tscript.compiler.impl.generation.writers.InstructionWriter;

public class BuildType extends AddressedInstruction {

    public BuildType(int address) {
        super(address);
    }

    @Override
    public void write(InstructionWriter writer) {
        writer.writeBuildType(this);
    }
}
