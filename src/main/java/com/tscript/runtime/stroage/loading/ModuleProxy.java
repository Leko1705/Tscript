package com.tscript.runtime.stroage.loading;

import com.tscript.runtime.stroage.FunctionArea;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.stroage.Pool;
import com.tscript.runtime.stroage.TypeArea;
import com.tscript.runtime.typing.Function;
import com.tscript.runtime.typing.Member;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

public class ModuleProxy implements Module {

    public Module module;

    private final Pool pool;

    public ModuleProxy(Pool pool) {
        this.pool = pool;
    }

    @Override
    public Member loadMember(int index) {
        return module.loadMember(index);
    }

    @Override
    public String getPath() {
        return module.getPath();
    }

    @Override
    public String getCanonicalPath() {
        return module.getCanonicalPath();
    }

    @Override
    public int getMinorVersion() {
        return module.getMinorVersion();
    }

    @Override
    public int getMajorVersion() {
        return module.getMajorVersion();
    }

    @Override
    public boolean storeUsedName(String name, TObject value) {
        return module.storeUsedName(name, value);
    }

    @Override
    public TObject loadUsedName(String name) {
        return module.loadUsedName(name);
    }

    @Override
    public Function getEntryPoint() {
        return module.getEntryPoint();
    }

    @Override
    public boolean isEvaluated() {
        return module.isEvaluated();
    }

    @Override
    public void setEvaluated() {
        module.setEvaluated();
    }

    @Override
    public Pool getPool() {
        return pool;
    }

    @Override
    public FunctionArea getFunctionArea() {
        return module.getFunctionArea();
    }

    @Override
    public TypeArea getTypeArea() {
        return module.getTypeArea();
    }

    @Override
    public Type getType() {
        return module.getType();
    }

    @Override
    public Iterable<Member> getMembers() {
        return module.getMembers();
    }
}
