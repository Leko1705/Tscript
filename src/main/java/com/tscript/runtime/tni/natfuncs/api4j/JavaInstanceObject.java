package com.tscript.runtime.tni.natfuncs.api4j;

import com.tscript.runtime.core.TscriptVM;
import com.tscript.runtime.typing.Member;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

class JavaInstanceObject implements TObject {

    private final JavaType type;
    protected final Object instance;

    private final List<Member> content = new ArrayList<>();

    protected JavaInstanceObject(JavaType type, TscriptVM vm) {
        this.type = type;

        Class<?> clazz = type.clazz;
        try {
            instance = clazz.getConstructor().newInstance();
        }
        catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

        init(vm);
    }

    protected JavaInstanceObject(JavaType type, Object instance, TscriptVM vm) {
        this.type = Objects.requireNonNull(type);
        this.instance = Objects.requireNonNull(instance);
        init(vm);
    }

    private void init(TscriptVM vm) {
        Class<?> clazz = type.clazz;

        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                content.add(new JavaField(field, instance, vm));
            }
        }

        Map<String, JavaMethod> methods = new LinkedHashMap<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
                JavaMethod javaMethod = methods.computeIfAbsent(method.getName(), name -> new JavaMethod(name, this));
                javaMethod.extend(method);
            }
        }

        content.addAll(methods.values());

    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Iterable<Member> getMembers() {
        return content;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

}

