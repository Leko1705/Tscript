package com.tscript.runtime.jit.ir;

import com.tscript.runtime.jit.ir.nodes.Goto;
import com.tscript.runtime.jit.ir.nodes.LoadLocal;
import com.tscript.runtime.jit.ir.nodes.Phi;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class IRBlock implements Iterable<IRNode> {

    public static final IRBlock ENDLESS_LOOP = new IRBlock(new AtomicInteger(0));

    static {
        ENDLESS_LOOP.append(new Goto(ENDLESS_LOOP));
    }

    private final List<IRNode> code;
    protected final SsaState ssa;

    public IRBlock(SsaState ssa) {
        this.code = new ArrayList<>();
        this.ssa = ssa;
    }

    public IRBlock(AtomicInteger nextSsa) {
        this(new SsaState(nextSsa));
    }

    public List<IRNode> getCode() {
        return code;
    }

    public void append(IRNode node) {
        code.add(node);
    }

    public int getSsa(int realAddress) {
        return ssa.getSsa(realAddress);
    }

    public int updateSsa(int address) {
        return ssa.updateSsa(address);
    }

    public void mapTransitionFrom(IRBlock sourceBlock) {
        ssa.mapTransitionFrom(sourceBlock);
    }

    public int getInitialSsa(int realAddress) {
        return ssa.getInitialSsa(realAddress);
    }

    public Phi getPhi(int address) {
        return ssa.getPhi(address);
    }

    public Collection<Integer> getRealAddresses() {
        return ssa.getRealAddresses();
    }

    @Override
    public Iterator<IRNode> iterator() {
        return code.iterator();
    }

}
