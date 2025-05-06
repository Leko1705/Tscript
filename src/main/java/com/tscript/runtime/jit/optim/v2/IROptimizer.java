package com.tscript.runtime.jit.optim.v2;

import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.typing.Type;

public interface IROptimizer {

    default IRBlock optimize(IRBlock block, Type[] inputTypes){
        return optimize(block);
    }

    IRBlock optimize(IRBlock block);

    boolean isOptimized();

}
