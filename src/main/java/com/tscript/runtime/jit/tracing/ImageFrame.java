package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.Frame;
import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.core.VirtualType;
import com.tscript.runtime.jit.JITSensitive;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.typing.Callable;
import com.tscript.runtime.typing.Function;
import com.tscript.runtime.typing.TObject;

import java.util.Map;

public class ImageFrame {

    public static ImageFrame fakeFrame(Callable callable, ImageFrame prevFrame) {
        return new ImageFrame(callable.getName(), prevFrame);
    }


    public int sp, ip;
    public String name;
    public Module module;
    @JITSensitive public int line;
    public byte[][] instructions;
    @JITSensitive public TObject[] locals;
    @JITSensitive public TObject[] stack;
    @JITSensitive public TObject owner;
    public int nextSafeSpot = -1;
    public ImageFrame next;
    public Map<String, TObject> names = null;


    private ImageFrame(String name, ImageFrame prevFrame) {
        this.name = name;
        this.next = prevFrame;
    }


    public ImageFrame(VirtualFunction vfunc, TObject[] args) {
        this.module = vfunc.getModule();
        this.name = vfunc.getName();
        this.ip = 0;
        this.sp = 0;
        this.stack = new TObject[vfunc.getStackSize()];
        this.instructions = vfunc.getInstructions();
        this.locals = new TObject[vfunc.getLocals()];
        this.line = -1;
        this.owner = vfunc.getOwner();
        System.arraycopy(args, 0, locals, 0, args.length);
    }

    public ImageFrame(Frame frame) {
        this.sp = frame.sp;
        this.ip = frame.ip;
        this.name = frame.getName();
        this.module = frame.getModule();
        this.line = frame.line();
        this.instructions = frame.getInstructions();
        this.locals = frame.locals;

        this.stack = frame.stack;
        this.owner = frame.getOwner();
        if (!frame.safeSpots.isEmpty()){
            this.nextSafeSpot = frame.safeSpots.element();
        }
    }

    public ImageFrame(VirtualFunction function, ImageFrame frame, int extraLocals) {
        this.sp = 0;
        this.ip = 0;
        this.name = function.getName();
        this.module = function.getModule();
        this.line = frame.line;
        this.instructions = function.getInstructions();
        this.locals = new TObject[function.getLocals() + extraLocals];
        this.stack = new TObject[function.getStackSize()];
        this.owner = function.getOwner();
        this.nextSafeSpot = frame.nextSafeSpot;

        this.next = frame;
    }

}
