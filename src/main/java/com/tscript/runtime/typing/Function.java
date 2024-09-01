package com.tscript.runtime.typing;



public abstract class Function implements Callable {

    private static final Type TYPE = new Type.Builder("Function").setConstructor((thread, params) -> Null.INSTANCE).build();


    private TObject owner;

    public TObject getOwner() {
        return owner;
    }

    public void setOwner(TObject owner) {
        this.owner = owner;
    }

    public abstract Function dup();

    @Override
    public  String getDisplayName() {
        return "Function<" + getName() + ">";
    }

    @Override
    public final  Type getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
