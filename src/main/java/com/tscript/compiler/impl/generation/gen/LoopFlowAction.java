package com.tscript.compiler.impl.generation.gen;

import com.tscript.compiler.impl.generation.compiled.instruction.AddressedInstruction;

public interface LoopFlowAction {

    AddressedInstruction getInstruction();

    boolean isBreak();

    record BreakAction(AddressedInstruction instruction) implements LoopFlowAction {

        @Override
        public AddressedInstruction getInstruction() {
            return instruction;
        }

        @Override
        public boolean isBreak() {
            return true;
        }
    }

    record ContinueAction(AddressedInstruction instruction) implements LoopFlowAction {

        @Override
        public AddressedInstruction getInstruction() {
            return instruction;
        }

        @Override
        public boolean isBreak() {
            return false;
        }
    }
}
