package com.tscript.tscriptc.analyze.scoping;

public interface ClassScope extends NestedScope {

    String getClassName();

    Scope getSuperClassScope();

}
