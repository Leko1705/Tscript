package com.tscript.runtime.typing;


import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TReal extends PrimitiveObject<Double> {

    public static final Type TYPE =
            new Type.Builder("Real")
                    .addMember(new Member(Visibility.PUBLIC, false, "nan", new GetNanStaticMethod()))
                    .addMember(new Member(Visibility.PUBLIC, false, "inf", new GetInfiniteStaticMethod()))
                    .setParameters(Parameters.newInstance().add("value", null))
                    .setConstructor((thread, params) -> {
                        TObject value = params.get(0);

                        if (value instanceof TReal)
                            return value;

                        if (value instanceof TInteger integer){
                            return new TReal(integer.getValue().doubleValue());
                        }

                        if (value instanceof TString string){
                            try {
                                return new TReal(Double.parseDouble(string.getValue()));
                            }
                            catch (NumberFormatException e){
                                thread.reportRuntimeError("can not convert string '" + string.getValue() + "' to Real");
                                return null;
                            }
                        }

                        thread.reportRuntimeError("can not convert " + value.getDisplayName() + " to Real");
                        return null;
                    }).build();

    private final Map<String, Member> methods;

    public TReal(Double value) {
        super(value);
        methods = new HashMap<>(Map.of(
                "isFinite", new Member(Visibility.PUBLIC, false, "isFinite", new IsFiniteMethod()),
                "isInfinite", new Member(Visibility.PUBLIC, false, "isInfinite", new IsInfiniteMethod()),
                "isNan", new Member(Visibility.PUBLIC, false, "isNan", new IsNanMethod())
        ));
    }

    @Override
    public  Type getType() {
        return TYPE;
    }

    @Override
    public Iterable<Member> getMembers() {
        return methods.values();
    }

    @Override
    public Member loadMember(String name) {
        return methods.get(name);
    }

    private class IsFiniteMethod extends NativeFunction {
        @Override
        public String getName() {
            return "isFinite";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            return TBoolean.of(!getValue().isInfinite());
        }
    }


    private class IsInfiniteMethod extends NativeFunction {
        @Override
        public String getName() {
            return "isInfinite";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            return TBoolean.of(getValue().isInfinite());
        }
    }


    private class IsNanMethod extends NativeFunction {
        @Override
        public String getName() {
            return "isNan";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            return TBoolean.of(getValue().isNaN());
        }
    }


    private static class GetNanStaticMethod extends NativeFunction {

        private static final TReal NAN = new TReal(Double.NaN);

        @Override
        public String getName() {
            return "nan";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            return NAN;
        }
    }

    private static class GetInfiniteStaticMethod extends NativeFunction {

        private static final TReal INF = new TReal(Double.POSITIVE_INFINITY);

        @Override
        public String getName() {
            return "inf";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            return INF;
        }
    }
}
