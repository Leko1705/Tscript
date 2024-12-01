package com.tscript.runtime.jit;

import com.tscript.runtime.jit.instructions.Instruction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface JITCompiler {

    CompletableFuture<NativeImage> compile(List<Instruction> instructions);

}
