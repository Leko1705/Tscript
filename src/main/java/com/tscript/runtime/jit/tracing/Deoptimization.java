package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.VirtualFrame;
import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.jit.caching.ObjectCache;
import com.tscript.runtime.jit.comp.ir.FrameState;
import com.tscript.runtime.typing.Member;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Returned by a native image instead of a regular result if compiled code was left at a guard
 * or trace exit (deoptimization). Carries the interpreter state that has to be restored to
 * continue execution in the interpreter. Never visible to tscript programs.
 * <p>
 * The values are ordered as described by {@link FrameState}: first the state of the frame the
 * trace belongs to (root frame), then the states of inlined frames (if any).
 */
public final class Deoptimization implements TObject {

    private static final Type TYPE = Type.newBuilder("Deoptimization")
            .setConstructor((env, params) -> { throw new AssertionError(); })
            .setAbstract(true)
            .build();

    public final DeoptInfo info;
    public final TObject[] values;

    public Deoptimization(DeoptInfo info, TObject[] values) {
        this.info = info;
        this.values = values;
    }

    /**
     * Writes the captured state of the root frame into the given interpreter frame.
     */
    public void applyTo(VirtualFrame frame) {
        apply(info.state().root(), frame);
    }

    private void apply(FrameState state, VirtualFrame frame) {
        frame.ip = state.ip();
        frame.setLine(state.line());

        int offset = state.offset() + (state.isRoot() ? 0 : 1);
        int[] indices = state.localIndices();
        for (int i = 0; i < indices.length; i++) {
            frame.locals[indices[i]] = values[offset + i];
        }
        offset += indices.length;

        for (int i = 0; i < frame.sp; i++) {
            frame.stack[i] = null;
        }
        System.arraycopy(values, offset, frame.stack, 0, state.stackSize());
        frame.sp = state.stackSize();
    }

    /**
     * Writes the captured local variables of the root frame into the given locals array.
     */
    public void applyTo(TObject[] frameLocals) {
        int[] indices = info.state().root().localIndices();
        for (int i = 0; i < indices.length; i++) {
            frameLocals[indices[i]] = values[i];
        }
    }

    /**
     * @return the captured operand stack of the root frame
     */
    public TObject[] rootStack() {
        FrameState root = info.state().root();
        TObject[] stack = new TObject[root.stackSize()];
        System.arraycopy(values, root.localIndices().length, stack, 0, stack.length);
        return stack;
    }

    public boolean hasInlinedFrames() {
        return !info.state().isRoot();
    }

    /**
     * Materializes the frames of inlined functions (callers first).
     */
    public List<VirtualFrame> inlinedFrames() {
        List<FrameState> states = new ArrayList<>();
        for (FrameState s = info.state(); !s.isRoot(); s = s.caller()) {
            states.add(0, s);
        }
        List<VirtualFrame> frames = new ArrayList<>(states.size());
        for (FrameState state : states) {
            VirtualFunction.VirtualMetadata metadata = (VirtualFunction.VirtualMetadata) ObjectCache.getObject(state.metadataId());
            TObject owner = values[state.offset()];
            VirtualFrame frame = new VirtualFrame(metadata, owner);
            apply(state, frame);
            frames.add(frame);
        }
        return frames;
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
