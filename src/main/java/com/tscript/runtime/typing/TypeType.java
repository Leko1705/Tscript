package com.tscript.runtime.typing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TypeType implements Type {

    private final Map<String, Member> members = new HashMap<>(Map.of(
            "superclass", Member.of(Visibility.PUBLIC, false, "superclass", new SuperClassStaticMethod()),
            "isOfType", Member.of(Visibility.PUBLIC, false, "isOfType", new IsOfTypeStaticMethod()),
            "isDerivedFrom", Member.of(Visibility.PUBLIC, false, "isDerivedFrom", new IsDerivedFromStaticMethod())
    ));

    @Override
    public String getName() {
        return "Type";
    }

    @Override
    public  Type getSuperType() {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public Map<String, Integer> getInstanceFields() {
        return Map.of();
    }

    @Override
    public Parameters getParameters(TThread thread) {
        return Parameters.newInstance()
                .add("value", null);
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public TObject eval(TThread thread, List<TObject> params) {
        return params.get(0).getType();
    }

    @Override
    public Type getType() {
        return this;
    }

    @Override
    public Iterable<Member> getMembers() {
        return members.values();
    }

    @Override
    public Member loadMember(String name) {
        return members.get(name);
    }


    private static class SuperClassStaticMethod extends NativeFunction {

        @Override
        public String getName() {
            return "superclass";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance()
                    .add("type", null);
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {

            TObject value = arguments.get(0);
            if (!(value instanceof Type type)) {
                env.reportRuntimeError("Type expected; got " + value.getType());
                return null;
            }

            Type superType = type.getSuperType();
            return superType != null ? superType : Null.INSTANCE;
        }
    }


    private class IsOfTypeStaticMethod extends NativeFunction {

        @Override
        public String getName() {
            return "isOfType";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance()
                    .add("value", null)
                    .add("type", null);
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {

            TObject value = arguments.get(0);

            TObject typeCandidate = arguments.get(1);
            if (!(typeCandidate instanceof Type type)) {
                env.reportRuntimeError("Type expected; got " + typeCandidate.getType());
                return null;
            }

            return ((NativeFunction)members.get("isDerivedFrom").get()).evaluate(env, List.of(value.getType(), type));
        }
    }


    private static class IsDerivedFromStaticMethod extends NativeFunction {

        @Override
        public String getName() {
            return "isDerivedFrom";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance()
                    .add("subclass", null)
                    .add("superclass", null);
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {

            TObject t1 = arguments.get(0);
            if (!(t1 instanceof Type subClass)) {
                env.reportRuntimeError("Type expected; got " + t1.getType());
                return null;
            }

            TObject t2 = arguments.get(1);
            if (!(t2 instanceof Type superClass)) {
                env.reportRuntimeError("Type expected; got " + t1.getType());
                return null;
            }

            Type curr = subClass;
            while (curr != null){
                if (curr == superClass)
                    return TBoolean.TRUE;
                curr = curr.getSuperType();
            }

            return TBoolean.FALSE;
        }
    }
}
