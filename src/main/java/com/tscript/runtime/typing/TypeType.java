package com.tscript.runtime.typing;

import com.tscript.runtime.core.TThread;



import java.util.List;
import java.util.Map;

class TypeType implements Type {

    @Override
    public  String getName() {
        return "Type";
    }

    @Override
    public  Type getSuperType() {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public Map<String, Integer> getInstanceFields() {
        return Map.of();
    }

    @Override
    public  Parameters getParameters(TThread thread) {
        return Parameters.newInstance();
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public  TObject eval(TThread thread, List<TObject> params) {
        return this;
    }

    @Override
    public  Type getType() {
        return this;
    }

    @Override
    public  Iterable<Member> getMembers() {
        return List.of();
    }
}
