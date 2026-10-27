package com.tscript.runtime.jit;

public interface CompileTask {

    NativeImage run(JITCompiler compiler);
}
