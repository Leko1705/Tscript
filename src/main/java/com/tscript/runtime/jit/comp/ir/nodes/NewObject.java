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
        this.carriedIndices = null;
        this.carriedMethods = null;
    }

    /** the member indices of the inputs of an object carried by a loop phi, null otherwise */
    private final int[] carriedIndices;
    private final java.util.Map<Integer, IRValue> carriedMethods;

    /**
     * Creates the object a loop phi of objects was replaced by (see
     * {@link com.tscript.runtime.jit.comp.optim.ObjectPhiSplitting}). It is never allocated; its
     * members do not change and are the inputs (data members) and the given methods.
     */
    public NewObject(VirtualType type, int typeId, int[] indices, java.util.List<IRValue> values,
                     java.util.Map<Integer, IRValue> methods) {
        super(values);
        this.type = type;
        this.typeId = typeId;
        this.carriedIndices = indices;
        this.carriedMethods = methods;
    }

    public boolean isCarried() {
        return carriedIndices != null;
    }

    /**
     * @return the value of a member of a carried object
     */
    public IRValue carriedMember(int index) {
        for (int i = 0; i < carriedIndices.length; i++) {
            if (carriedIndices[i] == index) return input(i);
        }
        return carriedMethods.get(index);
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
