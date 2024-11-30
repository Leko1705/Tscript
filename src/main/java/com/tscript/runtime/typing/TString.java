package com.tscript.runtime.typing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;

import java.util.*;
import java.util.regex.Pattern;


public class TString extends PrimitiveObject<String>
        implements ContainerAccessibleObject, IterableObject{

    public static final TString EMPTY = new TString("");

    public static final Type TYPE =
            new Type.Builder("String")
                    .addMember(Member.of(Visibility.PUBLIC, false, "fromUnicode", new FromUnicodeStaticMethod()))
                    .addMember(Member.of(Visibility.PUBLIC, false, "join", new JoinStaticMethod()))
                    .setParameters(Parameters.newInstance().add("value", EMPTY))
                    .setConstructor((thread, params) -> new TString(TNIUtils.toString(thread, params.get(0))))
                    .build();

    public static final Type ITR_TYPE = new Type.Builder("StringIterator")
            .setConstructor((thread, params) ->  Null.INSTANCE)
            .setAbstract(true)
            .build();

    private final Map<String, Member> methods = new HashMap<>(Map.of(
            "size", Member.of(Visibility.PUBLIC, false, "size", new SizeMethod()),
            "find", Member.of(Visibility.PUBLIC, false, "find", new FindMethod()),
            "split", Member.of(Visibility.PUBLIC, false, "split", new SplitMethod()),
            "toUpperCase", Member.of(Visibility.PUBLIC, false, "toUpperCase", new ToUpperCaseMethod()),
            "toLowerCase", Member.of(Visibility.PUBLIC, false, "toLowerCase", new ToLowerCaseMethod()),
            "replace", Member.of(Visibility.PUBLIC, false, "replace", new ReplaceMethod())
    ));

    public TString(String value) {
        super(value);
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

    @Override
    public TObject readFromContainer(TThread thread, TObject key) {
        if (!(key instanceof TInteger || key instanceof Range)){
            thread.reportRuntimeError("invalid key " + key.getType() + ": <Integer> or <Range> expected");
            return null;
        }
        String content = getValue();
        if (key instanceof TInteger i) {
            int index = i.getValue();
            if (index < 0 || index >= content.length()) {
                thread.reportRuntimeError("index " + index + " out of bounds for length " + content.length());
                return null;
            }
            return new TString(Character.toString(content.charAt(index)));
        }
        else {
            Range range = (Range) key;
            int from = range.getFrom();
            if (from < 0) from = 0;
            int to = range.getTo();
            if (to > content.length()) to = content.length();
            String subString = content.substring(from, to);
            return new TString(subString);
        }
    }

    @Override
    public IteratorObject iterator() {
        return new TStringIterator();
    }

    private class TStringIterator implements IteratorObject {

        int i = 0;
        int len = TString.this.getValue().length();

        @Override
        public boolean hasNext() {
            return i < len;
        }

        @Override
        public TObject next() {
            char c = TString.this.getValue().charAt(i++);
            return new TString(Character.toString(c));
        }

        @Override
        public Type getType() {
            return ITR_TYPE;
        }

        @Override
        public Iterable<Member> getMembers() {
            return List.of();
        }
    }


    private class SizeMethod extends NativeFunction {

        @Override
        public String getName() {
            return "size";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            return new TInteger(getValue().length());
        }
    }

    private class FindMethod extends NativeFunction {

        private static final TInteger ZERO = new TInteger(0);

        @Override
        public String getName() {
            return "find";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance()
                    .add("searchterm", null)
                    .add("start", ZERO)
                    .add("backward", TBoolean.FALSE);
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {

            if (!(arguments.get(0) instanceof TString str)) {
                env.reportRuntimeError("required: String; got:" + arguments.get(0).getType());
                return null;
            }

            if (!(arguments.get(1) instanceof TInteger start)) {
                env.reportRuntimeError("required: Integer; got:" + arguments.get(1).getType());
                return null;
            }

            if (!(arguments.get(2) instanceof TBoolean backwards)) {
                env.reportRuntimeError("required: Boolean; got:" + arguments.get(2).getType());
                return null;
            }

            if (backwards.getValue()){
                int lastIndex = getValue().lastIndexOf(str.getValue(), start.getValue());
                return TBoolean.of(lastIndex != -1);
            }
            else {
                int index = getValue().indexOf(str.getValue(), start.getValue());
                return TBoolean.of(index != -1);
            }
        }
    }


    private class SplitMethod extends NativeFunction {

        @Override
        public String getName() {
            return "split";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance()
                    .add("separator", null);
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {

            if (!(arguments.get(0) instanceof TString separator)) {
                env.reportRuntimeError("required: String; got:" + arguments.get(0).getType());
                return null;
            }

            String[] split = getValue().split(Pattern.quote(separator.getValue()));
            List<TObject> arr = new ArrayList<>(split.length);
            for (String str : split) arr.add(new TString(str));
            return new TArray(arr);
        }

    }


    private class ToUpperCaseMethod extends NativeFunction {

        @Override
        public String getName() {
            return "toUpperCase";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            return new TString(getValue().toUpperCase());
        }
    }


    private class ToLowerCaseMethod extends NativeFunction {

        @Override
        public String getName() {
            return "toLowerCase";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance();
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {
            return new TString(getValue().toLowerCase());
        }
    }


    private class ReplaceMethod extends NativeFunction {

        @Override
        public String getName() {
            return "replace";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance()
                    .add("pattern", null)
                    .add("replacement", null);
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {

            if (!(arguments.get(0) instanceof TString pattern)) {
                env.reportRuntimeError("required: String; got:" + arguments.get(0).getType());
                return null;
            }

            if (!(arguments.get(1) instanceof TString replacement)) {
                env.reportRuntimeError("required: replacement; got:" + arguments.get(1).getType());
                return null;
            }

            return new TString(getValue().replace(pattern.getValue(), replacement.getValue()));
        }

    }


    private static class FromUnicodeStaticMethod extends NativeFunction {

        @Override
        public String getName() {
            return "fromUnicode";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance()
                    .add("characters", null);
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {

            if (!(arguments.get(0) instanceof TArray characters)) {
                env.reportRuntimeError("required: Array; got:" + arguments.get(0).getType());
                return null;
            }

            StringBuilder buf = new StringBuilder();

            for (TObject obj : characters.getValue()){
                if (!(obj instanceof TInteger unicode)) {
                    env.reportRuntimeError("array contents bust be type of Integer");
                    return null;
                }
                buf.append((char) unicode.getValue().intValue());
            }

            return new TString(buf.toString());
        }
    }


    private static class JoinStaticMethod extends NativeFunction {

        private static final TString EMPTY = new TString("");

        @Override
        public String getName() {
            return "join";
        }

        @Override
        public Parameters doGetParameters(Environment env) {
            return Parameters.newInstance()
                    .add("array", null)
                    .add("separator", EMPTY);
        }

        @Override
        public TObject evaluate(Environment env, List<TObject> arguments) {

            if (!(arguments.get(0) instanceof TArray array)) {
                env.reportRuntimeError("required: Array; got:" + arguments.get(0).getType());
                return null;
            }

            if (!(arguments.get(1) instanceof TString separator)) {
                env.reportRuntimeError("required: String; got:" + arguments.get(1).getType());
                return null;
            }

            StringJoiner joiner = new StringJoiner(separator.getValue());
            for (TObject val : array.getValue()){
                if (!(val instanceof TString strVal)) {
                    env.reportRuntimeError("array contents bust be type of String");
                    return null;
                }
                joiner.add(strVal.getValue());
            }

            return new TString(joiner.toString());
        }
    }


}
