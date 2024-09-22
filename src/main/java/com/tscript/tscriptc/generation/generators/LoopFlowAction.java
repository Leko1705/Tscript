package com.tscript.tscriptc.generation.generators;

import com.tscript.tscriptc.generation.compiled.instruction.AddressedInstruction;

public interface LoopFlowAction {

    AddressedInstruction getInstruction();

    boolean isBreak();

}
