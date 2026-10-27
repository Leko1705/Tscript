package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.utils.Conversion;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Finds the loop headers of a function: every target of a backward jump.
 */
public class LoopAnalysis {

    private static final Map<VirtualFunction.VirtualMetadata, Set<Integer>> cache = new ConcurrentHashMap<>();

    public static Set<Integer> loopHeaders(VirtualFunction.VirtualMetadata metadata) {
        return cache.computeIfAbsent(metadata, LoopAnalysis::analyze);
    }

    private static Set<Integer> analyze(VirtualFunction.VirtualMetadata metadata) {
        Set<Integer> headers = new HashSet<>();
        byte[][] instructions = metadata.instructions();
        for (int ip = 0; ip < instructions.length; ip++) {
            byte[] ins = instructions[ip];
            Opcode opcode = Opcode.of(ins[0]);
            if (opcode == Opcode.GOTO || opcode == Opcode.BRANCH_IF_TRUE || opcode == Opcode.BRANCH_IF_FALSE) {
                int target = Conversion.from2Bytes(ins[1], ins[2]);
                if (target <= ip) headers.add(target);
            }
        }
        return Collections.unmodifiableSet(headers);
    }

    /**
     * @return true if the instruction at the given address (skipping line markers) returns from the function
     */
    public static boolean isReturnAt(VirtualFunction.VirtualMetadata metadata, int ip) {
        byte[][] instructions = metadata.instructions();
        while (ip < instructions.length && Opcode.of(instructions[ip][0]) == Opcode.NEW_LINE) ip++;
        return ip < instructions.length && Opcode.of(instructions[ip][0]) == Opcode.RETURN;
    }
}
