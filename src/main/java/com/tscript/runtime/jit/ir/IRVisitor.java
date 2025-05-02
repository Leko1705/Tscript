package com.tscript.runtime.jit.ir;

import com.tscript.runtime.jit.ir.nodes.Int;

public interface IRVisitor<P, R> {

    R visitInteger(Int node, P p);


}
