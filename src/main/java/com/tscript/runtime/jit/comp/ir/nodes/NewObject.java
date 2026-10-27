package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.core.VirtualType;
import com.tscript.runtime.jit.comp.ir.*;

/**
 * Allocates an instance of a class (without running its constructor, see
 * {@link VirtualType#newInstance()}). If the instance does not escape the trace, the allocation
 * is removed and the object is only created when compiled code is left (see
 * {@link com.tscript.runtime.jit.comp.optim.ObjectScalarReplacement}).
 */
public class NewObject extends IRValue {

    public final VirtualType type;

    /** ObjectCache id of the type */
    public final int typeId;

    public NewObject(VirtualType type, int typeId) {
        this.type = type;
        this.typeId = typeId;
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    protected String name() {
        return "NewObject[" + type.getName() + "]";
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitNewObject(this, p);
    }
}
