package com.tscript.runtime.jit.comp.ir;

import com.tscript.runtime.jit.comp.ir.nodes.*;

public interface IRControlVisitor<P, R> {

    R visitStart(StartControl node, P p);

    R visitReturn(Return node, P p);

    R visitMerge(MergeControl node, P p);

    R visitIfElse(IfElse node, P p);

    R visitLoopHead(LoopHeadNode node, P p);

    R visitLoopEnd(LoopEndNode node, P p);

    R visitExit(Exit node, P p);

}
