package com.tscript.compiler.impl.generation.compiled;

import com.tscript.compiler.impl.utils.Visibility;

import java.util.List;

public interface CompiledClass extends CompiledUnit {

    int getIndex();

    String getName();

    int getSuperIndex();

    boolean isAbstract();

    Constructor getConstructor();

    int getStaticInitializerIndex();

    List<Member> getStaticMembers();

    List<Member> getInstanceMembers();


    class Member {

        public static Member of(int index, String name, Visibility visibility, boolean isMutable){
            return new Member(index, name, visibility, isMutable);
        }


        public final int index;
        public final String name;
        public final Visibility visibility;
        public final boolean isMutable;

        private Member(int index, String name, Visibility visibility, boolean isMutable) {
            this.index = index;
            this.name = name;
            this.visibility = visibility;
            this.isMutable = isMutable;
        }

    }


    record Constructor(Visibility visibility, int index) {
    }

}
