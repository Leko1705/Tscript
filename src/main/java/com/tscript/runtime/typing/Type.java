package com.tscript.runtime.typing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public interface Type extends Callable, Callable.Metadata {

    Type TYPE = new TypeType();

    static TypeBuilder newBuilder(String name) {
        return new TypeBuilder(name);
    }


   String getName();

   default String getId(){
       String id = "id<" + getName() + ">{";
       Type superType = getSuperType();
       if (superType != null) {
           id += superType.getId();
       }
       id += "" + isAbstract()
               + getInstanceFields().size();
       if (isVirtual()) id += ";V}";
       else id += ";N}";
       return id;
   }

    @Override
    default Type getType() {
        return TYPE;
    }

    @Override
    default String getDisplayName() {
        return "Type<" + getName() + ">";
    }


    Type getSuperType();

    boolean isAbstract();

   Map<String, Integer> getInstanceFields();

    @Override
    Parameters getParameters(TThread thread);

    @Override
    boolean isVirtual();

    @Override
    default Metadata getMetadata() {
        return this;
    }

    default Function getConstructor(){
        return null;
    }

    @Override
    TObject eval(TThread thread, List<TObject> params);

    @Override
    default String name() {
        return getName();
    }


}
