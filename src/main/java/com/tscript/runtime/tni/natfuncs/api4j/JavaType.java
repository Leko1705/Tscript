package com.tscript.runtime.tni.natfuncs.api4j;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.core.TscriptVM;
import com.tscript.runtime.typing.Member;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class JavaType implements Type {

    protected final Class<?> clazz;
    private final TscriptVM vm;
    private final List<Member> content = new ArrayList<>();

    protected JavaType(Class<?> clazz, TscriptVM vm) {
        this.clazz = clazz;
        this.vm = vm;

        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                content.add(new JavaField(field, this, vm));
            }
        }

        Map<String, JavaMethod> methods = new LinkedHashMap<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
                JavaMethod javaMethod = methods.computeIfAbsent(method.getName(), name -> new JavaMethod(name, new JavaInstanceObject(this, clazz, vm)));
                javaMethod.extend(method);
            }
        }

        content.addAll(methods.values());
    }

    @Override
    public String getName() {
        return clazz.getName();
    }

    @Override
    public Type getSuperType() {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    @Override
    public Map<String, Integer> getInstanceFields() {
        return Map.of();
    }

    @Override
    public Parameters getParameters(TThread thread) {
        return Parameters.newInstance();
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public TObject eval(TThread thread, List<TObject> params) {
        return new JavaInstanceObject(this, vm);
    }

    @Override
    public Type getType() {
        return Type.TYPE;
    }

    @Override
    public Iterable<Member> getMembers() {
        return content;
    }
}