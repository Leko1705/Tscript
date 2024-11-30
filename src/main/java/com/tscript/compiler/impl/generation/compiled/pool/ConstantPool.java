package com.tscript.compiler.impl.generation.compiled.pool;

import com.tscript.compiler.impl.generation.compiled.CompiledUnit;

import java.util.List;

public interface ConstantPool extends CompiledUnit {

    List<PoolEntry<?>> getEntries();

}
