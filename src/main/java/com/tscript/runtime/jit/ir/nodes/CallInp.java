package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CallInp implements IRValue {

    public IRValue called;
    public final List<IRValue> args;
    public IRType type = IRUnknownType.INSTANCE;

    public CallInp(IRValue called, List<IRValue> args) {
        this.called = called;
        this.args = args;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitCallInp(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {
        if (oldNode == called) {
            called = newNode;
            return true;
        }

        AtomicBoolean b = new AtomicBoolean(false);
        args.replaceAll(candidate -> {
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
