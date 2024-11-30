package com.tscript.runtime.typing;

import com.tscript.runtime.core.InternalRuntimeErrorMessages;
import com.tscript.runtime.core.TThread;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Override
    TObject eval(TThread thread, List<TObject> params);



    interface Constructor {
        TObject eval(TThread thread, List<TObject> params);
    }



    class Builder {

        private final String name;
        private boolean isAbstract = false;
        private final List<Member> members = new ArrayList<>();
        private Parameters parameters = Parameters.newInstance();
        private Type superType;
        private Constructor constructor;

        public Builder(String name) {
            this.name = Objects.requireNonNull(name);
        }

        public Builder setConstructor( Constructor constructor) {
            this.constructor = Objects.requireNonNull(constructor);
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
                throw new IllegalStateException("constructor not set");

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
                public TObject eval(TThread thread, List<TObject> params) {
                    if (isAbstract()){
                        thread.reportRuntimeError(InternalRuntimeErrorMessages.invalidAbstractInstantiation(getName()));
                        return null;
                    }
                    return constructor.eval(thread, params);
                }

                @Override
                public Type getType() {
                    return TYPE;
                }

                @Override
                public Iterable<Member> getMembers() {
                    return members;
                }
            };

        }
    }

}
