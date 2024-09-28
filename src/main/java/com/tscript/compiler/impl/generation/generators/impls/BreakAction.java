package com.tscript.compiler.impl.generation.generators.impls;

import com.tscript.compiler.impl.generation.compiled.instruction.AddressedInstruction;

public class BreakAction implements LoopControlFlowAction {

    private final AddressedInstruction instruction;

    public BreakAction(AddressedInstruction instruction) {
        this.instruction = instruction;
    }

    @Override
    public AddressedInstruction getInstruction() {
        return instruction;
    }

    @Override
    public boolean isBreak() {
        return true;
    }
}
