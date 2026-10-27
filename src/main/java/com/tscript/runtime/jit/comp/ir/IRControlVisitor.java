package com.tscript.runtime.jit.comp.ir;

import com.tscript.runtime.jit.comp.ir.nodes.IfElse;
import com.tscript.runtime.jit.comp.ir.nodes.MergeControl;
import com.tscript.runtime.jit.comp.ir.nodes.Return;
import com.tscript.runtime.jit.comp.ir.nodes.StartControl;
import com.tscript.runtime.jit.comp.ir.nodes.*;

public interface IRControlVisitor<P, R> {

    R visitStart(StartControl node, P p);

    R visitReturn(Return node, P p);

    R visitMerge(MergeControl node, P p);

    R visitIfElse(IfElse node, P p);

}
