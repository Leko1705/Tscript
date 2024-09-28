package com.tscript.compiler.impl.analyze.scoping;

public interface ClassScope extends NestedScope {

    String getClassName();

    Scope getSuperClassScope();

}
