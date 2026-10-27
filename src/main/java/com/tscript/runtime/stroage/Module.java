package com.tscript.runtime.stroage;

import com.tscript.runtime.typing.Function;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

public interface Module extends TObject {

    Type TYPE =
            Type.newBuilder("Module")
                    .setConstructor((env, params) -> {
                        throw new AssertionError("Module is abstract");
                    })
                    .setAbstract(true)
                    .build();

    String getPath();

    String getCanonicalPath();

    @Override
    default String getDisplayName() {
        return "Module<" + getCanonicalPath() + ">";
    }

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
