package com.tscript.runtime.stroage.loading;

import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.core.VirtualType;
import com.tscript.runtime.stroage.FunctionArea;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.stroage.Pool;
import com.tscript.runtime.stroage.TypeArea;
import com.tscript.runtime.typing.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadedModule implements Module {

    public static final Type TYPE =
            new Type.Builder("Module")
                    .setConstructor((thread, params) -> {
                        throw new AssertionError("Module is abstract");
                    })
                    .setAbstract(true)
                    .build();


    private final String path;
    private final String canonicalPath;
    private final int major;
    private final int minor;

    private final Pool pool;

    private final Member[] members;
    private final Map<String, Integer> namedMembers = new HashMap<>();

    private final Map<String, TObject> usedNames = new HashMap<>();

    private final int entryPoint;

    private boolean evaluated = false;


    private final FunctionArea functionArea;
    private final TypeArea typeArea;


    public LoadedModule(String path,
                        String canonicalPath,
                        int major,
                        int minor,
                        Pool pool,
                        Member[] members,
                        int entryPoint,
                        FunctionArea functionArea,
                        TypeArea typeArea) {
        this.path = path;
        this.canonicalPath = canonicalPath;
        this.major = major;
        this.minor = minor;
        this.pool = pool;
        this.members = members;
        for (int i = 0; i < members.length; i++) {
            namedMembers.put(members[i].name, i);
        }
        this.entryPoint = entryPoint;
        this.functionArea = functionArea;
        this.typeArea = typeArea;
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public Member loadMember(String name) {
        int index = namedMembers.getOrDefault(name, -1);
        if (index == -1) return null;
        return members[index];
    }

    @Override
    public Member loadMember(int index) {
        return members[index];
    }

    @Override
    public Iterable<Member> getMembers() {
        return List.of(members);
    }

    @Override
    public String getDisplayName() {
        return "Module<" + canonicalPath + ">";
    }

    public String getPath() {
        return path;
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }

    @Override
    public int getMinorVersion() {
        return minor;
    }

    @Override
    public int getMajorVersion() {
        return major;
    }
    

    public boolean storeUsedName(String name, TObject value) {
        return usedNames.put(name, value) != null;
    }

    public TObject loadUsedName(String name) {
        return usedNames.get(name);
    }

    public Pool getPool() {
        return pool;
    }

    @Override
    public FunctionArea getFunctionArea() {
        return functionArea;
    }

    @Override
    public TypeArea getTypeArea() {
        return typeArea;
    }


    public Function getEntryPoint() {
        return functionArea.loadFunction(entryPoint, this);
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    @Override
    public void setEvaluated() {
        evaluated = true;
    }

    @Override
    public String toString() {
        return canonicalPath;
    }
}
