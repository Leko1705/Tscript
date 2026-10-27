package com.tscript.runtime.core;

import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.typing.Function;
import com.tscript.runtime.typing.Member;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.utils.Conversion;
import com.tscript.runtime.utils.Tuple;


import java.util.ArrayList;
import java.util.List;

public class VirtualFunction extends Function {

    private final VirtualMetadata metadata;

    private final Parameters parameters;

    private final List<Member> members;

    public VirtualFunction(VirtualMetadata metadata, Parameters parameters) {
        this.parameters = parameters;
        this.metadata = metadata;
        this.members = new ArrayList<>();
    }

    private VirtualFunction(VirtualFunction vf) {
        this.metadata = vf.metadata;
        this.parameters = vf.parameters;
        this.members = vf.members;
    }

    @Override
    public String getName() {
        return metadata.name();
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
    public  TObject eval0(TThread thread, List<TObject> params) {
        VirtualFrame frame = new VirtualFrame(this);
        thread.frameStack.push(frame);
        for (int i = params.size() - 1; i >= 0; i--) {
            frame.push(params.get(i));
        }
        return null;
    }

    @Override
    public Iterable<Member> getMembers() {
        return members;
    }

    @Override
    public void addMember(Member member) {
        members.add(member);
    }

    @Override
    public Function dup() {
        VirtualFunction duplicate = new VirtualFunction(this);
        duplicate.setOwner(getOwner());
        return duplicate;
    }

    public Module getModule() {
        return metadata.module();
    }

    public int getLocals() {
        return metadata.locals();
    }

    public byte[][] getInstructions() {
        return metadata.instructions();
    }

    public int getStackSize() {
        return metadata.stackSize();
    }

    public VirtualMetadata getMetadata() {
        return metadata;
    }


    public static abstract class VirtualMetadata implements Metadata {

        public abstract Module module();

        public abstract String name();

        public abstract int stackSize();

        public abstract int locals();

        public abstract byte[][] instructions();

        public abstract Tuple<String, Integer>[] parameters();

        @Override
        public final boolean isVirtual() {
            return true;
        }

        public VirtualFunction create() {
            Parameters parameters = Parameters.newInstance();

            for (Tuple<String, Integer> parameter : parameters()) {
                int poolAddr = parameter.getSecond();
                TObject value = null;
                if (poolAddr != -1) {
                    byte[] addrArr = Conversion.to2Bytes(poolAddr);
                    value = module().getPool().loadConstant(addrArr[0], addrArr[1]);
                }
                parameters.add(parameter.getFirst(), value);
            }

            return new VirtualFunction(this, parameters);
        }

    }

}
