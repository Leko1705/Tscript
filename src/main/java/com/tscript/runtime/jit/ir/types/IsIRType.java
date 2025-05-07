package com.tscript.runtime.jit.ir.types;

class IsIRType extends IRTypeVisitorImpl<Class<? extends IRType>, Boolean> {
    @Override
    public Boolean defaultOperation(IRType type, Class<? extends IRType> aClass) {
        return type.getClass() == aClass;
    }
}