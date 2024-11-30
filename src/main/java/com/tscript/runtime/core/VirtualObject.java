package com.tscript.runtime.core;

import com.tscript.runtime.typing.Member;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.Arrays;

public class VirtualObject implements TObject {

    protected TObject superObject = null;
    protected VirtualObject subObject = null;
    private final Type type;
    private final Member[] members;

    public VirtualObject(Type type, Member[] members) {
        this.type = type;
        this.members = members;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public TObject getSuper() {
        return superObject;
    }

    @Override
    public Member loadMember(String name) {
        int index = type.getInstanceFields().getOrDefault(name, -1);
        if (index == -1) return null;
        return members[index];
    }

    @Override
    public Iterable<Member> getMembers() {
        return Arrays.asList(members);
    }

}
