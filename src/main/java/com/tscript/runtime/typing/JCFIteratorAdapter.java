package com.tscript.runtime.typing;

import java.util.Iterator;
import java.util.List;

public class JCFIteratorAdapter implements IteratorObject {

    private final Type type;
    private final Iterator<TObject> itr;

    public JCFIteratorAdapter(Type type, Iterator<TObject> itr) {
        this.type = type;
        this.itr = itr;
    }

    @Override
    public TObject next() {
        return itr.next();
    }

    @Override
    public boolean hasNext() {
        return itr.hasNext();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Iterable<Member> getMembers() {
        return List.of();
    }
}
