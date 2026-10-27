package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.types.ComplexType;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Checks whether a value is of a given (primitive) type. Used by type guards.
 */
public class IsType extends UnaryOp {

    public final IRType type;

    public IsType(IRValue value, IRType type) {
        super(value);
        this.type = type;
    }

    @Override
    protected boolean dataEquals(IRValue other) {
        return type.equals(((IsType) other).type);
    }

    @Override
    protected int dataHashCode() {
        return type.hashCode();
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitIsType(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRType actual = tool.type(getValue());
        if (IRType.isExact(actual)) {
            return tool.builder().bool(actual.equals(type));
        }
        if (actual instanceof ComplexType && type instanceof ComplexType) {
            // values only get a class type if their type is known exactly (type guards,
            // allocations, specialized entries)
            return tool.builder().bool(actual.equals(type));
        }
        return this;
    }

    @Override
    public String toString() {
        return name() + "#" + id + "(#" + getValue().id + " is " + type + ")";
    }
}
