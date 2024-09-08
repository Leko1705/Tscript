package com.tscript.tscriptc.generation.compiled.pool;

import com.tscript.tscriptc.generation.compiled.CompiledUnit;

import java.util.List;

public interface ConstantPool extends CompiledUnit {

    List<PoolEntry<?>> getEntries();

}
