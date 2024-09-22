package com.tscript.tscriptc.generation.generators;

import com.tscript.tscriptc.generation.compiled.instruction.AddressedInstruction;

public record BreakAction(AddressedInstruction instruction) implements LoopFlowAction {

    @Override
    public AddressedInstruction getInstruction() {
        return instruction;
    }

    @Override
    public boolean isBreak() {
        return true;
    }
}
