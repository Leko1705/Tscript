package com.tscript.tscriptc.generation.generators.impls;

import com.tscript.tscriptc.generation.compiled.instruction.AddressedInstruction;

public interface LoopControlFlowAction {

    AddressedInstruction getInstruction();

    boolean isBreak();

}
