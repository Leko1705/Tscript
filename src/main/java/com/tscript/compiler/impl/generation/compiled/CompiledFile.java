package com.tscript.compiler.impl.generation.compiled;

import com.tscript.compiler.impl.generation.compiled.pool.ConstantPool;
import com.tscript.compiler.source.utils.Version;

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
