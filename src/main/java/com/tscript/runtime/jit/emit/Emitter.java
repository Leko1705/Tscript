package com.tscript.runtime.jit.emit;

import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.ir.IRBlock;

public interface Emitter {

    NativeImage emit(IRBlock block);

}
