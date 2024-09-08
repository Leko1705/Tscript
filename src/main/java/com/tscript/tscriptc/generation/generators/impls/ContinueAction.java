package com.tscript.tscriptc.generation.generators.impls;

import com.tscript.tscriptc.generation.compiled.instruction.AddressedInstruction;

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
