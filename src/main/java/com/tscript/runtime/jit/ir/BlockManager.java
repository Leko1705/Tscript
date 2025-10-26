package com.tscript.runtime.jit.ir;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockManager {

    private IRBlock currentBlock;

    private final Map<Integer, IRBlock> blockaddrsses;

    private final AtomicInteger nextFreeSsa;

    public BlockManager() {
        this.nextFreeSsa = new AtomicInteger(0);
        this.currentBlock = new IRBlock(nextFreeSsa);
        this.blockaddrsses = new HashMap<>();
    }

    public IRBlock computeBlockForAddress(int ip) {
        return blockaddrsses.computeIfAbsent(ip, k -> new IRBlock(nextFreeSsa));
    }

    public IRBlock requireBlockForAddress(int ip) {
        return Objects.requireNonNull(blockaddrsses.get(ip), "no block for jump address " + ip);
    }

    public boolean hasBlockForAddress(int ip) {
        return blockaddrsses.containsKey(ip);
    }

    public void associateAddress(int ip, IRBlock block) {
        blockaddrsses.put(ip, block);
    }

    public IRBlock currentBlock() {
        return currentBlock;
    }

    public IRBlock setCurrentBlock(IRBlock currentBlock) {
        this.currentBlock = currentBlock;
        return currentBlock;
    }

    public Collection<IRBlock> getAllBlocks() {
        return Set.copyOf(blockaddrsses.values());
    }

    public AtomicInteger getNextFreeSsa() {
        return nextFreeSsa;
    }

}
