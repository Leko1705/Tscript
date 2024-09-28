package com.tscript.compiler.impl.generation.compiled;

import com.tscript.compiler.impl.analyze.structures.Visibility;

import java.util.List;

public interface CompiledClass extends CompiledUnit {

    int getIndex();

    String getName();

    int getSuperIndex();

    boolean isAbstract();

    int getConstructorIndex();

    int getStaticInitializerIndex();

    List<Member> getStaticMembers();

    List<Member> getInstanceMembers();


    class Member {

        public static Member of(String name, Visibility visibility, boolean isMutable){
            return new Member(name, visibility, isMutable);
        }


        public final String name;
        public final Visibility visibility;
        public final boolean isMutable;

        private Member(String name, Visibility visibility, boolean isMutable) {
            this.name = name;
            this.visibility = visibility;
            this.isMutable = isMutable;
        }

    }

}
