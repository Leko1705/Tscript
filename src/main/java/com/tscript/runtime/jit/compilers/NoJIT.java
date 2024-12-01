package com.tscript.runtime.jit.compilers;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.instructions.Instruction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NoJIT implements JITCompiler {
    @Override
    public CompletableFuture<NativeImage> compile(List<Instruction> instructions) {
        return new CompletableFuture<>();
    }
}
