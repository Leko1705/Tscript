package runtime.type;

import runtime.core.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class PrimitiveObject<T> extends BaseObject {

    private final T value;

    public PrimitiveObject(T value) {
        super(List.of());
        this.value = value;
    }

    public PrimitiveObject(T value, List<Member> members) {
        super(members);
        this.value = value;
    }

    void initLater(List<Member> members){
        content = members.toArray(new Member[0]);
        keys.clear();
        int i = 0;
        for (Member member : content) keys.put(member.name, i++);
    }

    public T get(){
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PrimitiveObject<?> p && Objects.equals(value, p.value);
    }

    @Override
    public String toString() {
        return Objects.toString(value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
