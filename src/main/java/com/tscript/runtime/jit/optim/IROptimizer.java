package com.tscript.runtime.jit.optim;

import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.typing.Type;

public interface IROptimizer {

    IRBlock optimize(IRBlock block, Type[] inputTypes);

    boolean isOptimized();

}
