package com.tscript.runtime.tni.natfuncs.api4j;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

class JavaMethod extends NativeFunction implements Member {

    private final String name;
    private final Object instance;
    private final JavaMethodTree methodTree;

    protected JavaMethod(String name, JavaInstanceObject object) {
        this.name = name;
        this.instance = object.instance;
        methodTree = new JavaMethodTree();

        setOwner(object);
    }

    protected void extend(Method method) {
        methodTree.add(method);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.PUBLIC;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public TObject get() {
        return this;
    }

    @Override
    public void set(TObject value, Environment env) {
        throw new IllegalAccessError();
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance();
    }

    @Override
    public TObject call(TThread thread, List<TObject> arguments) {
        return evaluate(thread, arguments);
    }

    @Override
    public TObject call(TThread thread, List<String> names, List<TObject> arguments) {
        return evaluate(thread, arguments);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {

        Method method = methodTree.getMethod(arguments.iterator());

        if (method == null) {
            String msg = "can not resolve method with signature: " + name;
            StringJoiner joiner = new StringJoiner(", ", "(", ")");
            for (TObject argument : arguments) {
                joiner.add(argument.getType().getName());
            }
            msg += joiner.toString();
            env.reportRuntimeError(msg);
            return null;
        }

        Object result;

        try {
            Object[] args = prepareParameters(method, arguments);
            result = method.invoke(instance, args);
        }
        catch (Exception e){
            env.reportRuntimeError("error while running java method: " + e.getClass().getName() + " -> " + e.getMessage());
            return null;
        }

        return APIUtils.toTObject(result, env.getCurrentThread().getVM());
    }

    private Object[] prepareParameters(Method method, List<TObject> args){
        Class<?>[] expectedTypes = method.getParameterTypes();
        Object[] prepared = new Object[expectedTypes.length];

        Iterator<TObject> iterator = args.iterator();
        for (int i = 0; i < expectedTypes.length; i++) {
            Class<?> expected = expectedTypes[i];
            TObject got = iterator.next();

            if (expected.isPrimitive() && got == Null.INSTANCE) {
                throw new RuntimeException(APIUtils.primitiveNotNullableError(expected));
            }

            Object arg = APIUtils.validate(expected, got);
            if (arg == null && got != Null.INSTANCE) {
                throw new RuntimeException(APIUtils.invalidType(expected, got));
            }

            prepared[i] = arg;
        }

        return prepared;
    }

}
