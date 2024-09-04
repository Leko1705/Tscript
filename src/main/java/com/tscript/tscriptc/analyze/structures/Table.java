package com.tscript.tscriptc.analyze.structures;

public interface Table {

    void moveTopLevel();

    void enterScope(Object context);

    void leaveScope();

}
