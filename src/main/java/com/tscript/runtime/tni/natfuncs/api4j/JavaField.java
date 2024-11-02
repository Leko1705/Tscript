package com.tscript.runtime.tni.natfuncs.api4j;

import com.tscript.runtime.core.TscriptVM;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.typing.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class JavaField implements TObject, Member {

    private final Field field;
    private final JavaType type;
    private final Object instance;
    private final TscriptVM vm;

    protected JavaField(Field field, Object instance, TscriptVM vm) {
        this.field = field;
        this.vm = vm;
        type = new JavaType(field.getType(), vm);
        this.instance = instance;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Iterable<Member> getMembers() {
        return get().getMembers();
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Visibility getVisibility() {
        return APIUtils.visibility(field.getModifiers());
    }

    @Override
    public boolean isMutable() {
        return !Modifier.isFinal(field.getModifiers());
    }

    @Override
    public TObject get() {
        try {
            return APIUtils.toTObject(field.get(instance), vm);
        }
        catch (Exception e){
            throw new AssertionError(e.getMessage());
        }
    }

    @Override
    public void set(TObject value, Environment env) {

        if (field.getType().isPrimitive() && value == Null.INSTANCE) {
            env.reportRuntimeError(APIUtils.primitiveNotNullableError(field.getType()));
            return;
        }

        Object candidate = APIUtils.validate(field.getType(), value);
        if (candidate == null && value != Null.INSTANCE) {
            env.reportRuntimeError(APIUtils.invalidType(field.getType(), value));
            return;
        }
        try {
            field.set(instance, candidate);
        }
        catch (Exception e){
            throw new AssertionError(e.getMessage());
        }
    }
}
