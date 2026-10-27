package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Invokes a callable with in-place arguments. Calls are not inlined; at runtime the callee is
 * dispatched dynamically (compiled callees run their native image).
 */
public class Call extends IRValue {

    /** ObjectCache id of the {@link com.tscript.runtime.jit.SourceSite} of this call */
    public final int site;

    public Call(IRValue called, List<IRValue> args, int site) {
        super(concat(called, args));
        this.site = site;
    }

    private static List<IRValue> concat(IRValue called, List<IRValue> args) {
        List<IRValue> all = new ArrayList<>(args.size() + 1);
        all.add(called);
        all.addAll(args);
        return all;
    }

    public IRValue getCalled() {
        return input(0);
    }

    public List<IRValue> getArgs() {
        return getInputs().subList(1, inputCount());
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitCall(this, p);
    }
}
