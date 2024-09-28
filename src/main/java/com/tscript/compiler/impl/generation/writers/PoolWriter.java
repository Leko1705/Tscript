package com.tscript.compiler.impl.generation.writers;

import com.tscript.compiler.impl.generation.compiled.pool.ConstantPool;

public interface PoolWriter {

    void writePool(ConstantPool pool);

}
