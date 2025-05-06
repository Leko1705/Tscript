package com.tscript.runtime.jit.optim.v2;

import com.tscript.runtime.jit.ir.*;
import com.tscript.runtime.jit.ir.nodes.LoadLocal;
import com.tscript.runtime.jit.ir.nodes.StoreLocal;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class TypeInferrer extends IRScanner<Void, Void> implements IROptimizer {

    private Deque<Map<Integer, IRType>> localsStack;

    @Override
    public IRBlock optimize(IRBlock block) {


        IRWalker<Void, Void> walker = new IRTreeWalker<>() {
            @Override
            public void onBlockEnter(IRBlock block) {
                localsStack = new ArrayDeque<>();
                localsStack.push(new HashMap<>());
            }
        };
        walker.walk(this, block);

        return block;
    }

    @Override
    public boolean isOptimized() {
        return false;
    }

    @Override
    public Void visitStoreLocal(StoreLocal node, Void unused) {
        super.visitStoreLocal(node, unused);
        getLocals().put(node.ssaAddress, node.value.getType());
        return null;
    }

    @Override
    public Void visitLoadLocal(LoadLocal node, Void unused) {
        node.type = getLocals().getOrDefault(node.ssaAddress, IRUnknownType.INSTANCE);
        return null;
    }

    private Map<Integer, IRType> getLocals() {
        return localsStack.element();
    }
}
