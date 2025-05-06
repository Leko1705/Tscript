package com.tscript.runtime.jit.ir;

import com.tscript.runtime.jit.ir.nodes.Branch;
import com.tscript.runtime.jit.ir.nodes.Goto;
import com.tscript.runtime.jit.ir.nodes.ItrBranch;

import java.util.*;

public class IRTreeWalker<P, R> implements IRWalker<P, R> {

    private final Set<IRBlock> visited = new HashSet<>();

    private final Deque<Queue<IRBlock>> leftBranchesStack = new LinkedList<>();


    @Override
    public void walk(IRVisitor<P, R> visitor, IRBlock block, P p) {
        visited.clear();
        leftBranchesStack.clear();
        visitor = new MonitorVisitor(visitor);

        pushLeftBlocks(block);

        IRBlock nextBlock;
        while ((nextBlock = nextBlock()) != null) {
            handleBlock(visitor, nextBlock, p);
        }
    }

    public void onBlockEnter(IRBlock block) {
        // Overridable method
    }

    public void onBlockExit(IRBlock block) {
        // Overridable method
    }

    private IRBlock nextBlock() {
        while (!leftBranchesStack.isEmpty()) {
            Queue<IRBlock> queue = leftBranchesStack.element();
            while (!queue.isEmpty()) {
                IRBlock nextBlock = queue.remove();
                if (queue.isEmpty()) {
                    leftBranchesStack.remove();
                }
                if (!visited.contains(nextBlock)) {
                    return nextBlock;
                }
            }
        }
        return null;
    }

    private void pushLeftBlocks(IRBlock... blocks) {
        leftBranchesStack.add(new LinkedList<>(List.of(blocks)));
    }

    private void handleBlock(IRVisitor<P, R> visitor, IRBlock block, P p) {
        onBlockEnter(block);
        for (IRNode node : block) {
            node.accept(visitor, p);
        }
        onBlockExit(block);
        visited.add(block);
    }

    private class MonitorVisitor extends IRVisitorDecorator<P, R> {

        public MonitorVisitor(IRVisitor<P, R> visitor) {
            super(visitor);
        }

        @Override
        public R visitGoto(Goto node, P p) {
            R r = super.visitGoto(node, p);
            pushLeftBlocks(node.block);
            return r;
        }

        @Override
        public R visitBranch(Branch node, P p) {
            R r = super.visitBranch(node, p);
            pushLeftBlocks(node.noJumpBlock, node.jumpBlock);
            return r;
        }

        @Override
        public R visitItrBranch(ItrBranch node, P p) {
            R r = super.visitItrBranch(node, p);
            pushLeftBlocks(node.noJumpBlock, node.jumpBlock);
            return r;
        }
    }
}
