package com.tscript.runtime.typing;

import com.tscript.runtime.tni.Environment;

public interface Member {

    String getName();

    Visibility getVisibility();

    boolean isMutable();

    TObject get();

    void set(TObject value, Environment env);


    static Member of(Visibility visibility, boolean mutable, String name, TObject value) {

        return new Member() {

            private TObject content = value;

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Visibility getVisibility() {
                return visibility;
            }

            @Override
            public boolean isMutable() {
                return mutable;
            }

            @Override
            public TObject get() {
                return content;
            }

            @Override
            public void set(TObject value, Environment env) {
                this.content = value;
            }
        };
    }

    static Member copy(Member member) {
        return of(member.getVisibility(), member.isMutable(), member.getName(), member.get());
    }

}
