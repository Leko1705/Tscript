package com.tscript.runtime.type;

import com.tscript.runtime.core.Data;
import com.tscript.runtime.core.Reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface TObject extends Data {

    TType getType();

    Member get(int index);

    default Member get(String key){
        int idx = getIndex(key);
        if (idx < 0) return null;
        return get(idx);
    }

    int getIndex(String key);

    Iterable<Member> getMembers();

    default Collection<Reference> getReferences(){
        List<Reference> references = new ArrayList<>();
        for (Member member : getMembers()){
            if (member.data != null && member.data.isReference()){
                references.add(member.data.asReference());
            }
        }
        return references;
    }

    boolean equals(Object o);


}
