package com.tscript.runtime.jit.optim.v2;

import com.tscript.runtime.jit.ir.*;
import com.tscript.runtime.jit.ir.nodes.UnpackObj;
import com.tscript.runtime.jit.ir.nodes.WrapTObj;

public class WrapOptimizer extends IRParentDelegationScanner<Void> implements IROptimizer {

    private boolean optimized = false;

    @Override
    public IRBlock optimize(IRBlock block) {
        optimized = false;

        IRWalker<IRNode, Void> walker = new IRTreeWalker<>();
        walker.walk(this, block);

        return block;
    }

    @Override
    public boolean isOptimized() {
        return optimized;
    }

    @Override
    public Void visitWrapTObj(WrapTObj node, IRNode parent) {
        super.visitWrapTObj(node, parent);

        if (parent instanceof WrapTObj wrapParent) {
            optimized = wrapParent.replace(node, node.value);
            return null;
        }

        if (parent instanceof UnpackObj) {
            optimized = parent.replace(node, node.value);
        }

        if (!node.value.getType().isPrimitive()) {
            optimized = parent.replace(node, node.value);
        }

        return null;
    }

    @Override
    public Void visitUnpackObj(UnpackObj node, IRNode parent) {
        super.visitUnpackObj(node, parent);

        while (node.value instanceof UnpackObj child) {
            node.value = child.value;
            optimized = true;
        }

        if (parent instanceof WrapTObj) {
            optimized = parent.replace(node, node.value);
        }

        if (node.value.getType().isPrimitive()) {
            optimized = parent.replace(node, node.value);
        }

        return null;
    }
}
