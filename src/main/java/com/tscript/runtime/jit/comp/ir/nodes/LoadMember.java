package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * Reads a member of an object.
 */
public class LoadMember extends UnaryOp {

    public final MemberAccess access;

    /** the member index for {@link MemberAccess#FIELD} and {@link MemberAccess#INTERNAL} */
    public final int index;

    /** the member name for {@link MemberAccess#EXTERNAL} */
    public final String name;

    public LoadMember(IRValue object, MemberAccess access, int index, String name) {
        super(object);
        this.access = access;
        this.index = index;
        this.name = name;
    }

    public IRValue getObject() {
        return getValue();
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    protected String name() {
        return "LoadMember[" + (access == MemberAccess.EXTERNAL ? name : access + " " + index) + "]";
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitLoadMember(this, p);
    }
}
