package com.tscript.runtime.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseObject implements TObject {

    Member[] content;

    final Map<String, Integer> keys;


    public BaseObject(List<Member> members){
        content = members.toArray(new Member[0]);
        keys = new HashMap<>();
        int i = 0;
        for (Member member : content) keys.put(member.name, i++);
    }

    @Override
    public Member get(int index) {
        return content[index];
    }

    @Override
    public int getIndex(String key) {
        return keys.getOrDefault(key, -1);
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
