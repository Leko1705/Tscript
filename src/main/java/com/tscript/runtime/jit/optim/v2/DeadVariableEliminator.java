package com.tscript.runtime.jit.optim.v2;

import com.tscript.runtime.jit.ir.*;
import com.tscript.runtime.jit.ir.nodes.*;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRTypeType;
import com.tscript.runtime.jit.ir.types.IRTypeVisitor;
import com.tscript.runtime.jit.ir.types.IRTypeVisitorImpl;
import com.tscript.runtime.jit.optim.State;
import com.tscript.runtime.typing.Type;

import java.util.*;

public class DeadVariableEliminator extends IRScanner<Void, Void> implements IROptimizer {

    private boolean optimized;

    private final State<Map<Integer, List<Runnable>>> removalCandidates = new State<>(new HashMap<>());
    private final Map<Integer, List<Runnable>> temps = new HashMap<>();
    private final Deque<Boolean> canEliminate = new ArrayDeque<>();

    private IRBlock currentBlock;
    private final Queue<Runnable> optimizations = new LinkedList<>();

    @Override
    public IRBlock optimize(IRBlock block, Type[] inputTypes) {
        optimized = false;
        canEliminate.push(true);

        IRWalker<Void, Void> walker = new IRTreeWalker<>(){
            @Override
            public void onBlockEnter(IRBlock block) {
                currentBlock = block;
            }
        };
        walker.walk(this, block);

        removeUnusedLocals();
        removeUnusedTemps();

        while (!optimizations.isEmpty()) {
            Runnable optimization = optimizations.poll();
            optimization.run();
        }

        return block;
    }

    @Override
    public boolean isOptimized() {
        return optimized;
    }

    private void removeUnusedLocals(){
        for (List<Runnable> indices : removalCandidates.get().values()) {
            for (Runnable optimization : indices) {
                optimizations.add(optimization);
                optimized = true;
            }
            indices.clear();
        }
    }

    private void removeUnusedTemps(){
        for (List<Runnable> indices : temps.values()) {
            for (Runnable optimization : indices) {
                optimizations.add(optimization);
                optimized = true;
            }
            indices.clear();
        }
    }

    @Override
    public Void visitPushFrame(PushFrame node, Void unused) {
        super.visitPushFrame(node, unused);
        scan(node.called, null);
        removalCandidates.storeAndSet(new HashMap<>());

        IRTypeVisitor<Void, Boolean> isTypeChecker = new IRTypeVisitorImpl<>() {
            @Override
            public Boolean defaultOperation(IRType type, Void unused) {
                return false;
            }

            @Override
            public Boolean visitTypeType(IRTypeType typeType, Void unused) {
                return true;
            }
        };

        boolean isTypeInit = node.called.getType().accept(isTypeChecker, null);

        canEliminate.push(!isTypeInit);
        return null;
    }

    @Override
    public Void visitPopFrame(PopFrame node, Void unused) {
        removeUnusedLocals();
        removalCandidates.restore();
        canEliminate.remove();
        return null;
    }

    @Override
    public Void visitStoreLocal(StoreLocal node, Void unused) {
        scan(node.value, null);

        List<Runnable> optims = removalCandidates.get().computeIfAbsent(node.ssaAddress, k -> new LinkedList<>());
        optimizations.addAll(optims);

        IRValue value = node.value;

        if (value instanceof WrapTObj wrapper) {
            if (wrapper.value.getType().isPrimitive()){
                value = null;
            }
            else {
                value = wrapper.value;
            }
        }

        if (value instanceof LoadLocal l) {
            if (node.ssaAddress == l.ssaAddress && canEliminate.element()){
                optimizations.add(() -> currentBlock.getCode().remove(node));
                optimized = true;
                return null;
            }
        }

        if (!(node.value instanceof ItrNext)){
            optims.add(() -> currentBlock.getCode().remove(node));
        }

        return null;
    }

    @Override
    public Void visitLoadLocal(LoadLocal node, Void unused) {
        int address = node.ssaAddress;
        List<Runnable> optims = removalCandidates.get().computeIfAbsent(address, k -> new LinkedList<>());
        optims.clear();
        return null;
    }

    @Override
    public Void visitStoreTemp(StoreTemp node, Void unused) {
        scan(node.value, null);

        List<Runnable> optims = temps.computeIfAbsent(node.address, k -> new LinkedList<>());
        optimizations.addAll(optims);

        if (node.value instanceof LoadTemp l) {
            if (node.address == l.address && canEliminate.element()){
                optimizations.add(() -> currentBlock.getCode().remove(node));
                optimized = true;
                return null;
            }
        }

        optims.add(() -> currentBlock.getCode().remove(node));
        return null;
    }

    @Override
    public Void visitLoadTemp(LoadTemp node, Void unused) {
        int address = node.address;
        List<Runnable> optims = temps.computeIfAbsent(address, k -> new LinkedList<>());
        optims.clear();
        return null;
    }
}
