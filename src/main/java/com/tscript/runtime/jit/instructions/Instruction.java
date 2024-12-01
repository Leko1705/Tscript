package com.tscript.runtime.jit.instructions;

public interface Instruction {

    void handle(InstructionHandler handler);

}
