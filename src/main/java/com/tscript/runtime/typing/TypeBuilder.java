package com.tscript.runtime.typing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TypeBuilder {


    public interface Constructor {
        TObject eval(Environment env, List<TObject> args);
    }


    private final String name;
    private boolean isAbstract;
    private Type superType;
    private final List<Member> members;
    private Parameters parameters;
    private Constructor constructorImpl;


    protected TypeBuilder(String name) {
        this.name = Objects.requireNonNull(name);
        this.isAbstract = false;
        this.members = new ArrayList<>();
        this.parameters = Parameters.EMPTY;
    }

    public TypeBuilder setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
        return this;
    }

    public TypeBuilder addMember(Member member) {
        Objects.requireNonNull(member);
        members.add(member);
        return this;
    }

    public TypeBuilder setSuperType(Type superType) {
        this.superType = superType;
        return this;
    }

    public TypeBuilder setParameters(Parameters parameters) {
        this.parameters = Objects.requireNonNull(parameters);
        return this;
    }

    public TypeBuilder setConstructor(Constructor constructor) {
        this.constructorImpl = Objects.requireNonNull(constructor);
        return this;
    }

    public Type build() {
        return new NativeType();
    }


    private class NativeType implements Type {

        private final ConstructorImpl constructor = new ConstructorImpl();

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
            return Map.of();
        }

        @Override
        public Parameters getParameters(TThread thread) {
            return parameters;
        }

        @Override
        public boolean isVirtual() {
            return false;
        }

        @Override
        public Iterable<Member> getMembers() {
            return members;
        }

        @Override
        public Function getConstructor() {
            return constructor;
        }

        @Override
        public TObject eval(TThread thread, List<TObject> params) {
            return constructorImpl.eval(thread, params);
        }

    }


    private class ConstructorImpl extends Function implements Callable.Metadata {

        private final String constructorName = name + "@constructor";

        @Override
        public Function dup() {
            return new ConstructorImpl();
        }

        @Override
        public TObject eval0(TThread thread, List<TObject> params) {
            return constructorImpl.eval(thread, params);
        }

        @Override
        public Parameters getParameters(TThread thread) {
            return parameters;
        }

        @Override
        public Metadata getMetadata() {
            return this;
        }

        @Override
        public Iterable<Member> getMembers() {
            return List.of();
        }

        @Override
        public boolean isVirtual() {
            return false;
        }

        @Override
        public String name() {
            return constructorName;
        }
    }
}
