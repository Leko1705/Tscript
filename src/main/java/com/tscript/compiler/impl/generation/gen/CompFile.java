package com.tscript.compiler.impl.generation.gen;

import com.tscript.compiler.impl.generation.compiled.CompiledClass;
import com.tscript.compiler.impl.generation.compiled.CompiledFile;
import com.tscript.compiler.impl.generation.compiled.CompiledFunction;
import com.tscript.compiler.impl.generation.compiled.GlobalVariable;
import com.tscript.compiler.impl.generation.compiled.pool.ConstantPool;
import com.tscript.compiler.source.utils.Version;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompFile implements CompiledFile {

    public List<GlobalVariable> globals = new ArrayList<>();
    public String moduleName;
    public int entryPoint;
    public final Pool pool = new Pool();
    public final Set<CompFunc> functions = new HashSet<>();
    public final Set<CompClass> classes = new HashSet<>();

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public Version getVersion() {
        return new Version(1, 2);
    }

    @Override
    public int getEntryPoint() {
        return entryPoint;
    }

    @Override
    public List<GlobalVariable> getGlobalVariables() {
        return globals;
    }

    @Override
    public ConstantPool getConstantPool() {
        return pool;
    }

    @Override
    public List<CompiledFunction> getFunctions() {
        return new ArrayList<>(functions);
    }

    @Override
    public List<CompiledClass> getClasses() {
        return new ArrayList<>(classes);
    }
}
