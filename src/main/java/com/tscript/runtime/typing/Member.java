package com.tscript.runtime.typing;

public class Member {

    public final Visibility visibility;
    public final boolean mutable;
    public final String name;
    public TObject content;

    public Member(Visibility visibility, boolean mutable, String name) {
        this.visibility = visibility;
        this.mutable = mutable;
        this.name = name;
    }

    public Member(Visibility visibility, boolean mutable, String name, TObject content) {
        this(visibility, mutable, name);
        this.content = content;
    }

    public Member(Member member) {
        this(member.visibility, member.mutable, member.name, member.content);
    }

}
