package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * Creates a function object (LOAD_VIRTUAL, optionally followed by SET_OWNER). The optional input
 * is the owner; without an owner the function owns itself.
 */
public class NewFunction extends IRValue {

    /** ObjectCache id of the module whose function area holds the function */
    public final int moduleId;

    /** index of the function in the function area of the module */
    public final int index;

    /** ObjectCache id of the metadata of the function */
    public final int metadataId;

    public NewFunction(int moduleId, int index, int metadataId) {
        this.moduleId = moduleId;
        this.index = index;
        this.metadataId = metadataId;
    }

    public NewFunction(int moduleId, int index, int metadataId, IRValue owner) {
        super(owner);
        this.moduleId = moduleId;
        this.index = index;
        this.metadataId = metadataId;
    }

    /**
     * @return the owner or null if the function owns itself
     */
    public IRValue getOwner() {
        return inputCount() > 0 ? input(0) : null;
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitNewFunction(this, p);
    }
}
