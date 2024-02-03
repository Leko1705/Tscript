package runtime.type;

import runtime.core.Argument;
import runtime.core.Data;
import runtime.core.TThread;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Callable extends BaseObject {

    private static TType type;

    private Data owner = null;


    public Callable() {
        super(List.of());
    }

    public Callable(List<Member> members){
        super(members);
    }

    public Data call(TThread ctx, List<Argument> args){

        LinkedHashMap<String, Data> defaults = getParameters();
        String[] paramNames = defaults.keySet().toArray(new String[0]);

        if (args.size() > defaults.size()){
            ctx.reportRuntimeError("too many arguments given");
            return null;
        }
        LinkedHashMap<String, Data> finals = new LinkedHashMap<>();

        int index = 0;
        for (Argument arg : args){
            String ref = arg.name();
            if (ref != null){
                if (!defaults.containsKey(ref)){
                    ctx.reportRuntimeError("function '" +getName() + "' has no parameter '" + ref + "'");
                    return null;
                }
                boolean success = finals.put(ref, arg.data()) == null;
                if (!success) {
                    ctx.reportRuntimeError("parameter '" + ref + "' is already assigned");
                    return null;
                }
            }
            else {
                String nextParam = paramNames[index];
                boolean success = finals.put(nextParam, arg.data()) == null;
                if (!success) {
                    ctx.reportRuntimeError("parameter '" + nextParam + "' is already assigned");
                    return null;
                }
            }
            index++;
        }

        for (Map.Entry<String, Data> defaultParam : defaults.entrySet()){
            if (!finals.containsKey(defaultParam.getKey()) && defaultParam.getValue() != null)
                finals.put(defaultParam.getKey(), defaultParam.getValue());
        }

        if (finals.size() < defaults.size()){
            ctx.reportRuntimeError("too few arguments given");
            return null;
        }

        return eval(ctx, finals);
    }

    public abstract String getName();

    public abstract LinkedHashMap<String, Data> getParameters();

    public abstract Data eval(TThread caller, LinkedHashMap<String, Data> params);

    public final void setOwner(Data owner){
        this.owner = owner;
    }

    public Data getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return "<Function " + getName() + ">";
    }

    @Override
    public TType getType() {
        if (type == null)
            type = new TType("Function", null);
        return type;
    }

}
