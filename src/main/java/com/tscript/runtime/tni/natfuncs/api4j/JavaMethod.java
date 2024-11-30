package com.tscript.runtime.tni.natfuncs.api4j;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.typing.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.StringJoiner;

class JavaMethod extends NativeFunction implements Member {

    private final String name;
    private final JavaInstanceObject instance;
    private final JavaInvocationTree methodTree;

    protected JavaMethod(String name, JavaInstanceObject instance) {
        this.name = name;
        this.instance = instance;
        methodTree = new JavaInvocationTree();

        setOwner(instance);
    }

    protected void extend(Method method) {
        methodTree.add(new MethodInvocation(method));
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
        thread.reportRuntimeError(APIUtils.requirePositionalArgsMsg());
        return null;
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {

        Invokable invokable = methodTree.getMethod(arguments.iterator());

        if (invokable == null) {
            String msg = "can not resolve method with signature: " + instance.instance.getClass().getSimpleName() + "#" + name;
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
            Object[] args = APIUtils.prepareParameters(invokable, arguments);
            result = invokable.invoke(instance.instance, args);
        }
        catch (Throwable e){
            env.reportRuntimeError("error while running java method: " + e.getClass().getName() + " -> " + e.getMessage());
            return null;
        }

        return APIUtils.toTObject(result, env.getCurrentThread().getVM());
    }


    private record MethodInvocation(Method method) implements Invokable {

        @Override
        public Object invoke(Object obj, Object[] args) throws Throwable {
            try {
                return method.invoke(obj, args);
            }
            catch (InvocationTargetException ex){
                throw ex.getTargetException();
            }
        }

        @Override
        public Class<?>[] getParameterTypes() {
            return method.getParameterTypes();
        }
    }

}
