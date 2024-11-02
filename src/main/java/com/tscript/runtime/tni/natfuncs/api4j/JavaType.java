package com.tscript.runtime.tni.natfuncs.api4j;

import com.tscript.runtime.core.Frame;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.core.TscriptVM;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

class JavaType implements Type {

    protected final Class<?> clazz;
    private final TscriptVM vm;
    protected final List<Member> content = new ArrayList<>();
    private final JavaInvocationTree constructorTree;

    protected JavaType(Class<?> clazz, TscriptVM vm) {
        this.clazz = clazz;
        this.vm = vm;
        constructorTree = new JavaInvocationTree();
    }

    protected void init(){
        for (java.lang.reflect.Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            constructorTree.add(new ConstructorInvocation(constructor));
        }
        Map<String, JavaMethod> methods = new HashMap<>();
        init(clazz, methods);
        content.addAll(methods.values());
    }

    private void init(Class<?> clazz, Map<String, JavaMethod> methods){

        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                content.add(new JavaField(field, this, vm));
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
                JavaMethod javaMethod = methods.computeIfAbsent(method.getName(), name -> new JavaMethod(name, new JavaInstanceObject(this, clazz, vm)));
                javaMethod.extend(method);
            }
        }


        if (clazz.getSuperclass() != null) {
            init(clazz.getSuperclass(), methods);
        }
    }

    @Override
    public String getName() {
        return clazz.getName();
    }

    @Override
    public Type getSuperType() {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    @Override
    public Map<String, Integer> getInstanceFields() {
        return Map.of();
    }

    @Override
    public Parameters getParameters(TThread thread) {
        return Parameters.newInstance();
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public TObject call(TThread thread, List<TObject> arguments) {
       return TNIUtils.evalNativeContext(thread, this, () -> eval(thread, arguments));
    }

    @Override
    public TObject call(TThread thread, List<String> names, List<TObject> arguments) {
        thread.reportRuntimeError(APIUtils.requirePositionalArgsMsg());
        return null;
    }

    @Override
    public TObject eval(TThread thread, List<TObject> arguments) {

        Invokable invokable = constructorTree.getMethod(arguments.iterator());

        if (invokable == null) {
            String msg = "can not resolve constructor with signature: " + clazz.getSimpleName();
            StringJoiner joiner = new StringJoiner(", ", "(", ")");
            for (TObject argument : arguments) {
                joiner.add(argument.getType().getName());
            }
            msg += joiner.toString();
            thread.reportRuntimeError(msg);
            return null;
        }

        Object result;

        try {
            Object[] args = APIUtils.prepareParameters(invokable, arguments);
            result = invokable.invoke(null, args);
        }
        catch (InvocationTargetException e) {
            thread.reportRuntimeError("error while running java constructor of " + clazz.getSimpleName() + ": " + e.getClass().getName() + " -> " + e.getTargetException().getMessage());
            return null;
        }
        catch (Throwable e){
            thread.reportRuntimeError("error while running java constructor of " + clazz.getSimpleName() + ": " + e.getClass().getName() + " -> " + e.getMessage());
            return null;
        }

        return new JavaInstanceObject(this, result, vm);
    }

    @Override
    public Type getType() {
        return Type.TYPE;
    }

    @Override
    public Iterable<Member> getMembers() {
        return content;
    }


    private record ConstructorInvocation(java.lang.reflect.Constructor<?> constructor) implements Invokable {

        @Override
        public Object invoke(Object obj, Object[] args) throws Exception {
            return constructor.newInstance(args);
        }

        @Override
        public Class<?>[] getParameterTypes() {
            return constructor.getParameterTypes();
        }
    }
}