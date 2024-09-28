package com.tscript.compiler.impl.generation.generators.impls;

import com.tscript.compiler.impl.generation.compiled.instruction.AddressedInstruction;

public interface LoopControlFlowAction {

    AddressedInstruction getInstruction();

    boolean isBreak();

}
