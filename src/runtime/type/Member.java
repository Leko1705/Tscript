package runtime.type;

import runtime.core.Data;

public class Member {

    public Member(Member m){
        this(m.name, m.visibility, m.kind, m.data);
    }

    public Member(String name, Visibility v, Kind kind, Data data){
        this.name = name;
        this.visibility = v;
        this.kind = kind;
        this.data = data;
    }

    public enum Kind {
        MUTABLE,
        IMMUTABLE
    }

    public final String name;
    public Data data;
    public Visibility visibility;
    public Kind kind;

    @Override
    public String toString() {
        return "Member{" +
                "name='" + name + '\'' +
                ", data=" + data +
                ", visibility=" + visibility +
                ", kind=" + kind +
                '}';
    }
}
