package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class ExprStmt implements IRNode {

    public IRValue expr;

    public ExprStmt(IRValue expr) {
        this.expr = expr;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitExprStmt(this, p);
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {
        if (oldNode == expr) {
            expr = newNode;
            return true;
        }
        return false;
    }
}
