package runtime.type;

import runtime.core.Data;

public interface TObject extends Data {

    TType getType();

    Member get(int index);

    Member get(String key);

    Iterable<Member> getMembers();

    boolean equals(Object o);


}
