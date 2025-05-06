package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRArrayType;
import com.tscript.runtime.jit.ir.types.IRType;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Array implements IRValue {

    public final List<IRValue> content;

    public final IRArrayType type = new IRArrayType();

    public Array(List<IRValue> content) {
        this.content = content;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitArray(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {
        AtomicBoolean b = new AtomicBoolean(false);
        content.replaceAll(candidate -> {
            if (candidate == oldNode) {
                b.set(true);
                return newNode;
            }
            else {
                return candidate;
            }
        });
        return b.get();
    }
}
