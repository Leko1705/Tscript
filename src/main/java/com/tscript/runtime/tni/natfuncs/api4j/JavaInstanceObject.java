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

    protected JavaInstanceObject(JavaType type, Object instance, TscriptVM vm) {
        this.type = Objects.requireNonNull(type);
        this.instance = Objects.requireNonNull(instance);
        Map<String, JavaMethod> methods = new LinkedHashMap<>();
        init(type.clazz, vm, methods);
        content.addAll(methods.values());
    }

    private void init(Class<?> clazz, TscriptVM vm, Map<String, JavaMethod> methods) {

        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                content.add(new JavaField(field, instance, vm));
            }
        }


        for (Method method : clazz.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
                JavaMethod javaMethod = methods.computeIfAbsent(method.getName(), name -> new JavaMethod(name, this));
                javaMethod.extend(method);
            }
        }

        if (clazz.getSuperclass() != null) {
            init(clazz.getSuperclass(), vm, methods);
        }

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

