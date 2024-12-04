package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.Frame;
import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.typing.TObject;

public class ImageFrame {
    public String name;
    public Module module;
    public int line;
    public byte[][] instructions;
    public int stackSize;
    public TObject[] locals;
    public TObject owner;
    public int nextSafeSpot = -1;
    public ImageFrame next;

    public ImageFrame(Frame frame) {
        this.name = frame.getName();
        this.module = frame.getModule();
        this.line = frame.line();
        this.instructions = frame.getInstructions();
        this.stackSize = frame.stack.length;
        this.locals = frame.locals;
        this.owner = frame.getOwner();
        if (!frame.safeSpots.isEmpty()){
            this.nextSafeSpot = frame.safeSpots.element();
        }
    }

    public ImageFrame(VirtualFunction function, ImageFrame frame, int extraLocals) {
        this.name = function.getName();
        this.module = function.getModule();
        this.line = frame.line;
        this.stackSize = function.getStackSize();
        this.instructions = function.getInstructions();
        this.locals = new TObject[function.getLocals() + extraLocals];
        System.arraycopy(frame.locals, 0, locals, 0, frame.locals.length);
        this.owner = function.getOwner();
        this.nextSafeSpot = frame.nextSafeSpot;

        this.next = frame;
    }

}
