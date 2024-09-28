package com.tscript.compiler.impl.generation.generators;

import com.tscript.compiler.impl.generation.compiled.instruction.AddressedInstruction;

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
