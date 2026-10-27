package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Integer modulo (only defined on integers). Traps on division by zero, like the interpreter.
 */
public class Mod extends BinaryOp {

    public Mod(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public boolean canTrap() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitMod(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Int l && rhs instanceof Int r && r.value != 0) {
            return tool.builder().integer(l.value % r.value);
        }
        if (tool.isIntegerOf(rhs, 1) && tool.isInt(lhs)) {
            return tool.builder().integer(0);
        }
        return this;
    }
}
