package com.tscript.runtime.core;

import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.typing.Function;
import com.tscript.runtime.typing.Member;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;



import java.util.List;

public class VirtualFunction extends Function {

    private final String name;
    private final Parameters parameters;
    private final byte[][] instructions;
    private final int stackSize;
    private final int locals;
    private final Module module;

    public VirtualFunction(String name,
                           Parameters parameters,
                           byte[][] instructions,
                           int stackSize,
                           int locals,
                           Module module) {
        this.name = name;
        this.parameters = parameters;
        this.instructions = instructions;
        this.stackSize = stackSize;
        this.locals = locals;
        this.module = module;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Parameters getParameters(TThread thread) {
        return parameters;
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    public  TObject eval(TThread thread, List<TObject> params) {
        Frame frame = new Frame(getOwner(), name, instructions, stackSize, locals, module);
        thread.frameStack.push(frame);
        for (TObject param : params) {
            frame.push(param);
        }
        return null;
    }

    @Override
    public Member loadMember(int index) {
        return null;
    }

    @Override
    public Member loadMember(String name) {
        return null;
    }

    @Override
    public Iterable<Member> getMembers() {
        return List.of();
    }

    @Override
    public Function dup() {
        VirtualFunction duplicate = new VirtualFunction(name, parameters, instructions, stackSize, locals, module);
        duplicate.setOwner(getOwner());
        return duplicate;
    }

}