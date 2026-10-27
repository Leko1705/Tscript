package com.tscript.runtime.jit.comp.ir;

import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.nodes.*;

public interface IREffectVisitor<P, R> {

    R visitStart(StartEffect node, P p);

    R visitNewLine(NewLine node, P p);

    R visitGuardBool(GuardBool node, P p);

    R visitGuardFunctionIdentity(GuardFunctionIdentity node, P p);

    R visitValueEffect(ValueEffect node, P p);

    R visitMergeEffect(MergeEffect node, P p);

    R visitPushFrame(PushFrame node, P p);

    R visitPopFrame(PopFrame node, P p);

}
