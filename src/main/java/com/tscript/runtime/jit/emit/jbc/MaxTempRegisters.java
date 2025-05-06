package com.tscript.runtime.jit.emit.jbc;

import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.jit.ir.IRScanner;
import com.tscript.runtime.jit.ir.IRTreeWalker;
import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.nodes.LoadTemp;
import com.tscript.runtime.jit.ir.nodes.StoreTemp;

import java.util.concurrent.atomic.AtomicInteger;

public class MaxTempRegisters {

    public static int compute(IRBlock block) {
        IRTreeWalker<Void, Void> walker = new IRTreeWalker<>();

        AtomicInteger count = new AtomicInteger();
        IRVisitor<Void, Void> visitor = new IRScanner<>(){
            @Override
            public Void visitLoadTemp(LoadTemp node, Void unused) {
                count.set(Math.max(count.get(), node.address));
                return null;
            }
            @Override
            public Void visitStoreTemp(StoreTemp node, Void unused) {
                count.set(Math.max(count.get(), node.address));
                return null;
            }
        };

        walker.walk(visitor, block);

        return count.get();
    }

}
