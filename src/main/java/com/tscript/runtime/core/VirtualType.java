package com.tscript.runtime.core;

import com.tscript.runtime.jit.JITSensitive;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.*;

import java.util.*;

public class VirtualType implements Type {

    private final String modulePath;
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
                       Member[] instanceMembers,
                       String modulePath) {
        this.modulePath = modulePath;
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
        return getConstructor().getParameters(thread);
    }

    @Override
    public boolean isVirtual() {
        return getConstructor().isVirtual();
    }

    @Override
    public Metadata getMetadata() {
        return getConstructor().getMetadata();
    }

    @Override
    public synchronized TObject eval(TThread thread, List<TObject> params) {

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

        VirtualObject object = newInstance();
        if (constructor == null) return object;

        Function constructor = this.constructor.dup();
        constructor.setOwner(object);
        TObject result = constructor.eval(thread, params);
        // a virtual constructor runs in its own frame (pushed by eval) and returns the object
        // itself; returning it here as well would push it onto the stack of the constructor
        return constructor.isVirtual() ? result : object;
    }

    /**
     * Creates an uninitialized instance. Instances of classes that extend another class
     * consist of one object per class of the hierarchy (see {@link VirtualObject#getSuper()}),
     * the super objects are initialized by the super constructor calls.
     */
    @JITSensitive
    public VirtualObject newInstance() {
        VirtualObject object = new VirtualObject(this, prepareInstanceMembers());
        if (superType instanceof VirtualType virtualSuperType) {
            VirtualObject superObject = virtualSuperType.newInstance();
            object.superObject = superObject;
            superObject.subObject = object;
        }
        return object;
    }

    private Member[] prepareInstanceMembers(){
        Member[] members = new Member[instanceMembers.length];
        for (int i = 0; i < instanceMembers.length; i++)
            members[i] = Member.copy(instanceMembers[i]);
        return members;
    }

    @Override
    public Iterable<Member> getMembers() {
        return Arrays.asList(staticMembers);
    }

    /**
     * @return the declared constructor or null if the class has the default constructor
     */
    public Function getDeclaredConstructor() {
        return constructor;
    }

    public Visibility getConstructorVisibility() {
        return constructorVisibility;
    }

    /**
     * @return the initial value of the instance member at the given index (before the constructor runs)
     */
    public TObject getInstanceMemberTemplate(int index) {
        return instanceMembers[index].get();
    }

    public Function getConstructor() {
        return Objects.requireNonNullElseGet(constructor, this::defaultConstructor);
    }

    @Override
    public Member loadMember(int index) {
        return staticMembers[index];
    }

    public String getModulePath() {
        return modulePath;
    }

    @Override
    public String getId() {
        return Type.super.getId() + modulePath;
    }

    private Function defaultConstructor() {
        return new NativeFunction() {

            @Override
            public String getName() {
                return name + "@constructor";
            }

            @Override
            public Parameters doGetParameters(Environment env) {
                return Parameters.EMPTY;
            }

            @Override
            public TObject evaluate(Environment env, List<TObject> arguments) {
                return getOwner();
            }
        };
    }

}
