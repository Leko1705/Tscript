package com.tscript.runtime.tni.natfuncs.api4j;

public interface Invokable {

    Object invoke(Object obj, Object[] args) throws Throwable;

    Class<?>[] getParameterTypes();

}
