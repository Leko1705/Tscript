package com.tscript.runtime.jit.comp.ir.types;

import com.tscript.runtime.jit.comp.ir.IRType;

import java.util.List;

public interface ParameterizedType extends IRType {

    List<IRType> getParameters();

}
