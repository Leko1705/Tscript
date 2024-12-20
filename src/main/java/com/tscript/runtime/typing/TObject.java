package com.tscript.runtime.typing;

public interface TObject {

    
    Type getType();

    
    Iterable<Member> getMembers();


    default Member loadMember(int index){
        for (Member member : getMembers()) {
            if (index == 0) return member;
            index--;
        }
        return null;
    }

    
    default Member loadMember(String name){
        for (Member member : getMembers()) {
            if (member.getName().equals(name))
                return member;
        }
        return null;
    }

    default void addMember(Member member){
        throw new UnsupportedOperationException();
    }

    default TObject getSuper(){
        return null;
    }
    
    default String getDisplayName(){
        return "<" + getType().getName() + ">@" + hashCode();
    }

    String toString();

    boolean equals(Object o);

    int hashCode();

}
