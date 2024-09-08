package com.tscript.tscriptc.generation.compiled;

import com.tscript.tscriptc.generation.compiled.pool.ConstantPool;
import com.tscript.tscriptc.utils.Version;

import java.util.List;

public interface CompiledFile extends CompiledUnit {

    String getModuleName();

    Version getVersion();

    int getEntryPoint();

    List<GlobalVariable> getGlobalVariables();

    ConstantPool getConstantPool();

    List<CompiledFunction> getFunctions();

    List<CompiledClass> getClasses();



}
