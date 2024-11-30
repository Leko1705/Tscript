package com.tscript.runtime.core;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.typing.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualType implements Type {

    private final String name;
    protected Type superType;
    private boolean superTypeInitialized = false;
    protected boolean isAbstract;
    private final Function constructor;
    private final Visibility constructorVisibility;
    private final Member[] staticMembers;

    private final Member[] instanceMembers;
    private final Map<String, Integer> instanceMemberIndexMap = new HashMap<>();

    public VirtualType(String name,
                       boolean isAbstract,
                       Function constructor,
                       Visibility constructorVisibility,
                       Member[] staticMembers,
                       Member[] instanceMembers) {
        this.name = name;
        this.isAbstract = isAbstract;
        this.constructor = constructor;
        this.constructorVisibility = constructorVisibility;
        this.staticMembers = staticMembers;

        this.instanceMembers = instanceMembers;
        for (int i = 0; i < instanceMembers.length; i++) {
            Member member = instanceMembers[i];
            instanceMemberIndexMap.put(member.getName(), i);
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

        if (constructorVisibility != Visibility.PUBLIC){
            if (constructorVisibility == Visibility.PRIVATE){
                TObject caller = thread.getFrame().getOwner();
                if (caller == null || (caller.getType() != this && caller != this)){
                    thread.reportRuntimeError("constructor has private access");
                    return null;
                }
            }
            else if (constructorVisibility == Visibility.PROTECTED){
                boolean isValidAccess = false;

                TObject caller = thread.getFrame().getOwner();
                if (caller != null) {
                    Type type = caller.getType();
                    while (type != null) {
                        if (type == this) {
                            isValidAccess = true;
                            break;
                        }
                        type = type.getSuperType();
                    }
                }
                if (!isValidAccess){
                    thread.reportRuntimeError("constructor has protected access");
                    return null;
                }
            }
        }

        Member[] members = new Member[instanceMembers.length];
        for (int i = 0; i < instanceMembers.length; i++)
            members[i] = Member.copy(instanceMembers[i]);
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
