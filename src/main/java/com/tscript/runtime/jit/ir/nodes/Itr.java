package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRTypeDecorator;

public class Itr implements IRValue {

    public IRValue iterable;

    public Itr(IRValue iterable) {
        this.iterable = iterable;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitItr(this, p);
    }

    @Override
    public IRType getType() {
        return new IRTypeDecorator(iterable.getType()) {
            @Override
            public boolean isIterable() {
                return true;
            }
        };
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {
        if (oldNode == iterable) {
            iterable = newNode;
            return true;
        }
        return false;
    }
}
