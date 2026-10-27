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

    private static final Map<VirtualFunction.VirtualMetadata, Integer> inlineSizes = new ConcurrentHashMap<>();

    private static final Set<Opcode> INLINABLE = EnumSet.of(
            Opcode.PUSH_NULL, Opcode.PUSH_INT, Opcode.PUSH_BOOL, Opcode.LOAD_CONST, Opcode.PUSH_THIS,
            Opcode.LOAD_BUILTIN, Opcode.POP, Opcode.NEW_LINE, Opcode.LOAD_GLOBAL, Opcode.STORE_GLOBAL,
            Opcode.LOAD_LOCAL, Opcode.STORE_LOCAL, Opcode.CONTAINER_READ, Opcode.CONTAINER_WRITE,
            Opcode.ADD, Opcode.SUB, Opcode.MUL, Opcode.DIV, Opcode.IDIV, Opcode.MOD, Opcode.POW,
            Opcode.AND, Opcode.OR, Opcode.XOR, Opcode.NOT, Opcode.LT, Opcode.GT, Opcode.LEQ, Opcode.GEQ,
            Opcode.SHL, Opcode.SHR, Opcode.USHR, Opcode.EQUALS, Opcode.NOT_EQUALS, Opcode.NEG, Opcode.POS,
            Opcode.GET_TYPE, Opcode.MAKE_ARRAY, Opcode.MAKE_DICT, Opcode.MAKE_RANGE, Opcode.GOTO,
            Opcode.BRANCH_IF_TRUE, Opcode.BRANCH_IF_FALSE, Opcode.CALL_INPLACE, Opcode.RETURN, Opcode.DUP);

    /**
     * @return the number of instructions of the function if it may be inlined (it has no loops and
     * only instructions supported by the tracer), -1 otherwise
     */
    public static int inlineSize(VirtualFunction.VirtualMetadata metadata) {
        return inlineSizes.computeIfAbsent(metadata, m -> {
            if (!loopHeaders(m).isEmpty()) return -1;
            for (byte[] ins : m.instructions()) {
                if (!INLINABLE.contains(Opcode.of(ins[0]))) return -1;
            }
            return m.instructions().length;
        });
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
