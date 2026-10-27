package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

import java.util.Objects;

public class ToString extends UnaryOp {
    public ToString(IRValue value) {
        super(value);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitToString(this, p);
    }

    @Override
    public String toString() {
        return "str(" + getValue().toString() + ")";
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {

        IRValue value = getValue();

        if (value instanceof Literal<?> l) {
            return tool.builder().string(Objects.toString(l.value));
        }

        return this;
    }

}
