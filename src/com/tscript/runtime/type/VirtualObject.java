package com.tscript.runtime.type;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class VirtualObject extends BaseObject {

    private final TType type;

    public VirtualObject(TType type, List<Member> members) {
        super(members);
        this.type = type;
    }

    @Override
    public TType getType() {
        return type;
    }

}
