package com.tscript.runtime.jit.optim.v2;

import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.jit.ir.IRScanner;
import com.tscript.runtime.jit.ir.IRTreeWalker;
import com.tscript.runtime.jit.ir.IRWalker;
import com.tscript.runtime.jit.ir.nodes.*;

import java.util.LinkedList;
import java.util.Queue;

public class DeadCodeEliminator extends IRScanner<Void, Void> implements IROptimizer {

    private boolean optimized;

    private final Queue<Runnable> optimizations = new LinkedList<>();

    private IRBlock currentBlock;

    @Override
    public IRBlock optimize(IRBlock block) {
        optimized = false;

        IRWalker<Void, Void> walker = new IRTreeWalker<>() {
            @Override
            public void onBlockEnter(IRBlock block) {
                currentBlock = block;
            }
        };
        walker.walk(this, block);

        while (!optimizations.isEmpty()) {
            Runnable optimization = optimizations.poll();
            optimization.run();
            optimized = true;
        }

        return block;
    }

    @Override
    public boolean isOptimized() {
        return optimized;
    }

    @Override
    public Void visitGuard(Guard node, Void unused) {
        scan(node.condition, null);

        IRValue value = valueOf(node.condition);

        if (value instanceof Bool b && b.value) {
            optimizations.add(() -> currentBlock.getCode().remove(node));
        }

        return null;
    }


    private static IRValue valueOf(IRValue value) {
        if (value instanceof WrapTObj w) {
            return w.value;
        }
        if (value instanceof UnpackObj u) {
            return u.value;
        }
        return value;
    }
}
