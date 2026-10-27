package com.tscript.runtime.jit.comp.emit;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.comp.ir.Graph;

public interface Emitter {

    NativeImage emit(JITCompiler compiler, Graph graph);

}
