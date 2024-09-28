package com.tscript.compiler.impl.generation.generators.impls;

import com.tscript.compiler.impl.generation.compiled.instruction.AddressedInstruction;

public class ContinueAction implements LoopControlFlowAction {

    private final AddressedInstruction instruction;

    public ContinueAction(AddressedInstruction instruction) {
        this.instruction = instruction;
    }

    @Override
    public AddressedInstruction getInstruction() {
        return instruction;
    }

    @Override
    public boolean isBreak() {
        return false;
    }
}
