package com.tscript.runtime.jit.ir;

import com.tscript.runtime.jit.ir.nodes.LoadLocal;
import com.tscript.runtime.jit.ir.nodes.Phi;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SsaState {

    private final AtomicInteger nextSsa;

    private final Map<Integer, Integer> initialSsa;
    private final Map<Integer, Phi> phis;
    private final Map<Integer, Integer> realToSsa = new HashMap<>();

    public SsaState(AtomicInteger nextSsa) {
        this.nextSsa = nextSsa;
        this.initialSsa = new HashMap<>();
        this.phis = new HashMap<>();
    }

    public int getSsa(int realAddress) {
        if (realToSsa.containsKey(realAddress)) {
            return realToSsa.get(realAddress);
        }
        int ssa = nextSsa.incrementAndGet();
        realToSsa.put(realAddress, ssa);
        initialSsa.put(realAddress, ssa);
        return ssa;
    }

    public int updateSsa(int address) {
        int ssa = nextSsa.incrementAndGet();
        realToSsa.put(address, ssa);
        if (!initialSsa.containsKey(address)) {
            initialSsa.put(address, ssa);
        }
        return ssa;
    }

    public void mapTransitionFrom(IRBlock sourceBlock) {
        final SsaState dest = this;
        final SsaState src = sourceBlock.ssa;

        for (Integer realAddress : src.realToSsa.keySet()) {
            int ssaAddress = src.realToSsa.get(realAddress);
            Phi phi = dest.getPhi(realAddress);

            LoadLocal ll = new LoadLocal(ssaAddress, realAddress);
            phi.options.put(sourceBlock, ll);
        }
    }

    public int getInitialSsa(int realAddress) {
        return initialSsa.get(realAddress);
    }

    public Phi getPhi(int address) {
        return phis.computeIfAbsent(address, k -> new Phi());
    }

    public Collection<Integer> getRealAddresses() {
        return Collections.unmodifiableCollection(realToSsa.keySet());
    }

}
