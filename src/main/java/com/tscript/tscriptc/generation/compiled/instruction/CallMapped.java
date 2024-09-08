package com.tscript.tscriptc.generation.compiled.instruction;

import com.tscript.tscriptc.generation.writers.InstructionWriter;

public class CallMapped implements Instruction {

    public final int argc;

    public CallMapped(int argc) {
        this.argc = argc;
    }

    @Override
    public void write(InstructionWriter writer) {

    }
}
