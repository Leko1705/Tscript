package runtime.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseObject implements TObject {

    Member[] content;

    final Map<String, Member> keys;


    public BaseObject(List<Member> members){
        content = members.toArray(new Member[0]);
        keys = new HashMap<>();
        for (Member member : content) keys.put(member.name, member);
    }

    @Override
    public Member get(int index) {
        return content[index];
    }

    @Override
    public Member get(String key) {
        return keys.get(key);
    }

    @Override
    public String toString() {
        return "<" + getType().getName() + ">";
    }

    @Override
    public Iterable<Member> getMembers() {
        return List.of(content);
    }

}
