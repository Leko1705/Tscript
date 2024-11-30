package com.tscript.compiler.impl.generation.gen;

import com.tscript.compiler.impl.generation.compiled.CompiledClass;

import java.util.ArrayList;
import java.util.List;

public class CompClass implements CompiledClass {

    public final String name;
    private final int index;
    private final boolean isAbstract;
    public int superIndex;

    public Constructor constructor;
    public int staticIndex;

    public final List<Member> instanceMembers = new ArrayList<>();
    public final List<Member> staticMembers = new ArrayList<>();

    public CompClass(String name, int index, boolean isAbstract) {
        this.name = name;
        this.index = index;
        this.isAbstract = isAbstract;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getSuperIndex() {
        return superIndex;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public Constructor getConstructor() {
        return constructor;
    }

    @Override
    public int getStaticInitializerIndex() {
        return staticIndex;
    }

    @Override
    public List<Member> getStaticMembers() {
        return staticMembers;
    }

    @Override
    public List<Member> getInstanceMembers() {
        return instanceMembers;
    }
}
