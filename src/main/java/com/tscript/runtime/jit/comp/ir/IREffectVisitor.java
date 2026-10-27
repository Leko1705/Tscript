package com.tscript.runtime.jit.comp.ir;

import com.tscript.runtime.jit.comp.ir.nodes.*;

public interface IREffectVisitor<P, R> {

    R visitStart(StartEffect node, P p);

    R visitAnchor(Anchor node, P p);

    R visitGuardBool(GuardBool node, P p);

    R visitLoopBegin(LoopBegin node, P p);

    R visitStoreGlobal(StoreGlobal node, P p);

    R visitContainerWrite(ContainerWrite node, P p);

    R visitStoreMember(StoreMember node, P p);

}
