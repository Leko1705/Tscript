package com.tscript.runtime.stroage;

import com.tscript.runtime.typing.Function;
import com.tscript.runtime.typing.TObject;

public interface Module extends TObject {

    String getPath();

    String getCanonicalPath();

    int getMinorVersion();

    int getMajorVersion();

    boolean storeUsedName(String name, TObject value);

    TObject loadUsedName(String name);


    Function getEntryPoint();

    boolean isEvaluated();

    void setEvaluated();


    Pool getPool();

    FunctionArea getFunctionArea();

    TypeArea getTypeArea();

}
