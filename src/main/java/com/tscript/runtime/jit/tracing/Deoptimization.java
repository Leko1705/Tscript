package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.VirtualFrame;
import com.tscript.runtime.jit.comp.ir.FrameState;
import com.tscript.runtime.typing.Member;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.List;

/**
 * Returned by a native image instead of a regular result if compiled code was left at a guard
 * or trace exit (deoptimization). Carries the interpreter state that has to be restored to
 * continue execution in the interpreter. Never visible to tscript programs.
 */
public final class Deoptimization implements TObject {

    private static final Type TYPE = Type.newBuilder("Deoptimization")
            .setConstructor((env, params) -> { throw new AssertionError(); })
            .setAbstract(true)
            .build();

    public final DeoptInfo info;
    public final TObject[] locals;
    public final TObject[] stack;

    public Deoptimization(DeoptInfo info, TObject[] locals, TObject[] stack) {
        this.info = info;
        this.locals = locals;
        this.stack = stack;
    }

    /**
     * Writes the captured state into the given interpreter frame.
     */
    public void applyTo(VirtualFrame frame) {
        FrameState state = info.state();
        frame.ip = state.ip();
        frame.setLine(state.line());

        int[] indices = state.localIndices();
        for (int i = 0; i < indices.length; i++) {
            frame.locals[indices[i]] = locals[i];
        }

        for (int i = 0; i < frame.sp; i++) {
            frame.stack[i] = null;
        }
        System.arraycopy(stack, 0, frame.stack, 0, stack.length);
        frame.sp = stack.length;
    }

    /**
     * Writes the captured local variables into the given locals array.
     */
    public void applyTo(TObject[] frameLocals) {
        int[] indices = info.state().localIndices();
        for (int i = 0; i < indices.length; i++) {
            frameLocals[indices[i]] = locals[i];
        }
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public Iterable<Member> getMembers() {
        return List.of();
    }
}
