package com.tscript.runtime.jit.instructions;

public record PushInt(int value) implements Instruction {

    @Override
    public void handle(InstructionHandler handler) {
        handler.handlePushInt(this);
    }
}
