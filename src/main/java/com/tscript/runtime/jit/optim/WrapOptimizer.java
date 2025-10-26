package com.tscript.runtime.jit.optim;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.jit.ir.*;
import com.tscript.runtime.jit.ir.nodes.BinaryOp;
import com.tscript.runtime.jit.ir.nodes.UnwrapTObj;
import com.tscript.runtime.jit.ir.nodes.WrapTObj;
import com.tscript.runtime.jit.ir.types.IRIntegerType;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.typing.Type;

import java.util.Set;

public class WrapOptimizer extends IRParentDelegationScanner<Void> implements IROptimizer {

    private boolean optimized = false;

    @Override
    public IRBlock optimize(IRBlock block, Type[] inputTypes) {
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

        if (node.value instanceof WrapTObj child) {
            node.value = child.value;
            optimized = true;
        }

        if (node.value instanceof UnwrapTObj child) {
            optimized |= parent.replace(node, child.value);
        }

        if (node.value instanceof WrapTObj && node.value.isCallingInstruction()) {
            optimized |= parent.replace(node, node.value);
        }

        return null;
    }

    @Override
    public Void visitUnwrapTObj(UnwrapTObj node, IRNode parent) {
        super.visitUnwrapTObj(node, parent);

        if (node.value instanceof UnwrapTObj child) {
            node.value = child.value;
            optimized = true;
        }

        if (node.value instanceof WrapTObj child) {
            optimized |= parent.replace(node, child.value);
        }

        if (node.value.canBePrimitive()) {
            optimized |= parent.replace(node, node.value);
        }

        return null;
    }

    @Override
    public Void visitBinaryOp(BinaryOp node, IRNode parent) {
        super.visitBinaryOp(node, parent);

        IRType lhsType = node.lhs.getType();
        IRType rhsType = node.rhs.getType();

        if (lhsType.isType(IRIntegerType.class) && rhsType.isType(IRIntegerType.class)) {
            if (Set.of(Opcode.ADD, Opcode.SUB, Opcode.MUL, Opcode.DIV, Opcode.MOD).contains(node.operation)) {

                if (node.lhs instanceof WrapTObj w){
                    node.lhs = new UnwrapTObj(w.value);
                    optimized = true;
                }
                else if (!node.lhs.canBePrimitive() && !(node.lhs instanceof UnwrapTObj)) {
                    node.lhs = new UnwrapTObj(node);
                    optimized = true;
                }


                if (node.rhs instanceof WrapTObj w){
                    node.rhs = new UnwrapTObj(w.value);
                    optimized = true;
                }
                else if (!node.rhs.canBePrimitive() && !(node.rhs instanceof UnwrapTObj)){
                    node.rhs = new UnwrapTObj(node);
                    optimized = true;
                }

            }
        }

        return null;
    }

}
