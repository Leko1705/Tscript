package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.VirtualFunction;

/**
 * Identifies a loop: the function it belongs to and the address of its header.
 */
public record LoopKey(VirtualFunction.VirtualMetadata metadata, int headerIp) {

    @Override
    public String toString() {
        return "loop " + metadata.name() + "@" + headerIp;
    }
}
