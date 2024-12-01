package com.tscript.runtime.jit.instructions;

public class PushNull implements Instruction {

    public static final PushNull INSTANCE = new PushNull();

    private PushNull() {}

    @Override
    public void handle(InstructionHandler handler) {
        handler.handlePushNull(this);
    }
}
