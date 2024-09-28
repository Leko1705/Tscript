package com.tscript.compiler.impl.generation.generators;

import com.tscript.compiler.impl.generation.compiled.instruction.AddressedInstruction;

public interface LoopFlowAction {

    AddressedInstruction getInstruction();

    boolean isBreak();

}
