package com.tscript.runtime.typing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public interface Type extends Callable {

    Type TYPE = new TypeType();



   String getName();

    @Override
    default String getDisplayName() {
        return "Type<" + getName() + ">";
    }

    Type getSuperType();

    boolean isAbstract();

   Map<String, Integer> getInstanceFields();

    @Override
    Parameters getParameters(TThread thread);

    /**
     * Creates a new raw, non initialized, instance of this Type
     * @param env the execution environment
     * @return the new instantiated object, or null if an error occurred.
     */
    default TObject newInstance(Environment env){
        env.reportRuntimeError("type can not get instantiated " + getName());
        return null;
    }

    default Function getConstructor(){
        return null;
    }

    @Override
    default TObject eval(TThread thread, List<TObject> params){
        TObject object = newInstance(thread);
        if (object == null) return null;

        Function constructor = getConstructor();

        if (constructor != null) {
            constructor = constructor.dup();
            constructor.setOwner(object);
            constructor.eval(thread, params);
        }

        return object;
    }



    interface Constructor {
        TObject eval(Function func, TThread thread, List<TObject> params);
    }


    class Builder {

        private final String name;
        private boolean isAbstract = false;
        private final List<Member> members = new ArrayList<>();
        private Parameters parameters = Parameters.newInstance();
        private Type superType;
        private Function constructor;
        private Supplier<TObject> instanceMaker;

        public Builder(String name) {
            this.name = Objects.requireNonNull(name);
        }

        public Builder setConstructor(Constructor constructor) {
            Objects.requireNonNull(constructor);
            this.constructor = new NativeFunction() {
                @Override
                public String getName() {
                    return name + "@constructor";
                }

                @Override
                public Parameters doGetParameters(Environment env) {
                    return parameters;
                }

                @Override
                public TObject evaluate(Environment env, List<TObject> arguments) {
                    return constructor.eval(this, env.getCurrentThread(), arguments);
                }
            };
            return this;
        }

        public Builder setInstanceMaker(Supplier<TObject> instanceMaker) {
            this.instanceMaker = instanceMaker;
            return this;
        }

        public Builder addMember( Member member) {
            members.add(Objects.requireNonNull(member));
            return this;
        }

        public Builder setAbstract(boolean isAbstract) {
            this.isAbstract = isAbstract;
            return this;
        }

        public Builder setParameters( Parameters parameters) {
            this.parameters = Objects.requireNonNull(parameters);
            return this;
        }

        public Builder setSuperType(Type superType) {
            this.superType = superType;
            return this;
        }

        public Type build() {

            if (constructor == null)
                throw new IllegalStateException("constructor not set for " + name);

            if (instanceMaker == null)
                throw new IllegalStateException("instanceMaker not set for " + name);

            return new Type() {

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public Type getSuperType() {
                    return superType;
                }

                @Override
                public boolean isAbstract() {
                    return isAbstract;
                }

                @Override
                public Map<String, Integer> getInstanceFields() {
                    return Map.of();
                }

                @Override
                public Parameters getParameters(TThread thread) {
                    return parameters;
                }

                @Override
                public boolean isVirtual() {
                    return false;
                }

                @Override
                public Type getType() {
                    return TYPE;
                }

                @Override
                public Iterable<Member> getMembers() {
                    return members;
                }

                @Override
                public Function getConstructor() {
                    return constructor;
                }

                @Override
                public TObject newInstance(Environment env) {
                    return instanceMaker.get();
                }
            };

        }
    }

}
