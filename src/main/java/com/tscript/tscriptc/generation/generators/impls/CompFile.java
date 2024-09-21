package com.tscript.tscriptc.generation.generators.impls;

import com.tscript.tscriptc.generation.compiled.CompiledClass;
import com.tscript.tscriptc.generation.compiled.CompiledFile;
import com.tscript.tscriptc.generation.compiled.CompiledFunction;
import com.tscript.tscriptc.generation.compiled.GlobalVariable;
import com.tscript.tscriptc.generation.compiled.pool.ConstantPool;
import com.tscript.tscriptc.utils.Version;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompFile implements CompiledFile {

    public List<GlobalVariable> globals = new ArrayList<>();
    public String moduleName;
    public int entryPoint;
    public final Pool pool = new Pool();
    public final Set<CompiledFunction> functions = new HashSet<>();
    public final Set<CompiledClass> classes = new HashSet<>();

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

    public int getGlobalIndex(String name){
        int i = 0;
        for (GlobalVariable glob : globals){
            if (glob.name.equals(name)){
                return i;
            }
            i++;
        }
        return -1;
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
