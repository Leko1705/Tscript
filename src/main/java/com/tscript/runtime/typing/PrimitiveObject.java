package com.tscript.runtime.typing;



import com.tscript.runtime.jit.JITSensitive;

import java.util.List;
import java.util.Objects;

public abstract class PrimitiveObject<V> implements TObject {

    @JITSensitive
    protected V value;

    protected PrimitiveObject(V value) {
        this.value = value;
    }

    @JITSensitive
    public abstract Type getType();

    @Override
    public  Iterable<Member> getMembers() {
        return List.of();
    }

    public V getValue() {
        return value;
    }

    @Override
    public String getDisplayName() {
        return Objects.toString(value);
    }

    @Override
    public Member loadMember(String name) {
        return null;
    }

    @Override
    public Member loadMember(int index) {
        return null;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @JITSensitive
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrimitiveObject<?> that = (PrimitiveObject<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
