package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * Writes a member of an object.
 */
public class StoreMember extends IREffect {

    public final MemberAccess access;

    /** the member index for {@link MemberAccess#FIELD} and {@link MemberAccess#INTERNAL} */
    public final int index;

    /** the member name for {@link MemberAccess#EXTERNAL} */
    public final String name;

    /** ObjectCache id of the {@link com.tscript.runtime.jit.SourceSite} of this store */
    public final int site;

    public StoreMember(IRValue object, IRValue value, MemberAccess access, int index, String name,
                       IRControl control, int site) {
        super(control, object, value);
        this.access = access;
        this.index = index;
        this.name = name;
        this.site = site;
    }

    public IRValue getObject() {
        return input(0);
    }

    public IRValue getValue() {
        return input(1);
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitStoreMember(this, p);
    }
}
