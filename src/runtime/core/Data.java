package runtime.core;

import runtime.type.TObject;

public interface Data {

    default boolean isValue(){
        return this instanceof TObject;
    }

    default TObject asValue(){
        return (TObject) this;
    }

    default boolean isReference(){
        return this instanceof Reference;
    }

    default Reference asReference(){
        return (Reference) this;
    }

    boolean equals(Object o);

}
