package com.tscript.runtime.jit.compilers;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.instructions.Instruction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ParallelJIT implements JITCompiler {

    private final JITCompiler underlying;

    public ParallelJIT(JITCompiler underlying) {
        this.underlying = underlying;
    }

    @Override
    public CompletableFuture<NativeImage> compile(List<Instruction> instructions) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return underlying.compile(instructions).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
