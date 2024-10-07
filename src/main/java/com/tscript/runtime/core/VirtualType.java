package com.tscript.runtime.core;

import com.tscript.runtime.typing.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualType implements Type {

    private final String name;
    private Type superType;
    private boolean superTypeInitialized = false;
    protected boolean isAbstract;
    private final Function constructor;
    private final Member[] staticMembers;

    private final Member[] instanceMembers;
    private final Map<String, Integer> instanceMemberIndexMap = new HashMap<>();

    public VirtualType(String name,
                       boolean isAbstract,
                       Function constructor,
                       Member[] staticMembers,
                       Member[] instanceMembers) {
        this.name = name;
        this.isAbstract = isAbstract;
        this.constructor = constructor;
        this.staticMembers = staticMembers;

        this.instanceMembers = instanceMembers;
        for (int i = 0; i < instanceMembers.length; i++) {
            Member member = instanceMembers[i];
            instanceMemberIndexMap.put(member.name, i);
        }
    }

    public void setSuperType(Type superType) {
        if (superTypeInitialized)
            throw new ExecutionException(
                    "super type cannot be changed and is only initialized on file loading procedure");
        this.superType = superType;
        superTypeInitialized = true;
    }

    public void notifySuperTypeInitialized() {
        this.superTypeInitialized = true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getSuperType() {
        return superType;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public Map<String, Integer> getInstanceFields() {
        return instanceMemberIndexMap;
    }

    @Override
    public Parameters getParameters(TThread thread) {
        if (constructor == null) return Parameters.newInstance();
        return constructor.getParameters(thread);
    }

    @Override
    public boolean isVirtual() {
        return constructor.isVirtual();
    }

    @Override
    public synchronized TObject eval(TThread thread, List<TObject> params) {
        if (isAbstract){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.invalidAbstractInstantiation(name));
            return null;
        }

        Member[] members = new Member[instanceMembers.length];
        for (int i = 0; i < instanceMembers.length; i++)
            members[i] = new Member(instanceMembers[i]);
        VirtualObject object = new VirtualObject(this, members);

        if (constructor != null) {
            Function copy = constructor.dup();
            copy.setOwner(object);
            copy.eval(thread, params);
        }

        return null;
    }

    @Override
    public Type getType() {
        return Type.TYPE;
    }

    @Override
    public Iterable<Member> getMembers() {
        return Arrays.asList(staticMembers);
    }

    public Function getConstructor() {
        return constructor;
    }

    @Override
    public Member loadMember(int index) {
        return staticMembers[index];
    }

}
