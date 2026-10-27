package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Not extends UnaryOp {

    public Not(IRValue value) {
        super(value);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitNot(this, p);
    }

    @Override
    public String toString() {
        return "!" + getValue().toString();
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {

        IRValue value = getValue();

        if (value instanceof Int i) {
            return tool.builder().integer(~i.value);
        }

        if (tool.isBooleanOf(value, true)) {
            return tool.builder().bool(false);
        }

        if (tool.isBooleanOf(value, false)) {
            return tool.builder().bool(true);
        }

        if (value instanceof Not n) {
            return n.getValue();
        }

        return this;
    }
}
