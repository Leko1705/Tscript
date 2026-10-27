package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * The super object of an object (the part of a class instance that belongs to the super class).
 */
public class GetSuper extends UnaryOp {

    public GetSuper(IRValue object) {
        super(object);
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitGetSuper(this, p);
    }
}
