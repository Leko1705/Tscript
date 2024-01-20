package runtime.type;

import runtime.core.Argument;
import runtime.core.Data;
import runtime.core.TThread;

import java.util.*;

public class TType extends Callable {

    private static final TType TYPE_TYPE = new TType("Type", null);

    private final String name;

    private TType superType;

    private final boolean isAbstract;

    private final List<Member> classMembers;

    private Callable constructor;

    public TType(String name,
                 TType superType,
                 boolean isAbstract,
                 List<Member> statics,
                List<Member> members) {
        super(statics);
        this.name = name;
        this.superType = superType;
        this.isAbstract = isAbstract;
        this.classMembers = members;
    }

    protected TType(String name, TType superType) {
        this(name, superType, false, List.of(), List.of());
    }

    public TType getSuper() {
        return superType;
    }

    public void setSuperType(TType superType) {
        this.superType = superType;
    }

    @Override
    public String getName() {
        return name;
    }

    public Callable getConstructor() {
        return constructor;
    }

    public void setConstructor(Callable constructor) {
        this.constructor = constructor;
    }

    @Override
    public Data call(TThread ctx, List<Argument> args) {

        if (isAbstract){
            ctx.reportRuntimeError("<" + name + "> is abstract and can not get instantiated");
            return null;
        }

        VirtualObject object = new VirtualObject(this, classMembers());
        if (constructor != null) {
            constructor.setOwner(ctx.storeHeap(object));
            constructor.call(ctx, args);
        }

        return null;
    }

    private List<Member> classMembers(){
        List<Member> members = new ArrayList<>();
        for (Member member : classMembers)
            members.add(new Member(member));
        return members;
    }

    @Override
    public Data eval(TThread caller, LinkedHashMap<String, Data> params) {
        throw new IllegalStateException("'eval' should neve be called in TType directly -> constructor evaluated instead");
    }

    @Override
    public LinkedHashMap<String, Data> getParameters() {
        throw new IllegalStateException("see #eval");
    }


    @Override
    public TType getType() {
        return TYPE_TYPE;
    }

    @Override
    public String toString() {
        return "<Type " + name + ">";
    }

}
