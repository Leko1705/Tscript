package com.tscript.runtime.tni.natfuncs.api4j;

import com.tscript.runtime.core.TerminationListener;
import com.tscript.runtime.core.TscriptVM;
import com.tscript.runtime.typing.Member;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JavaAPIStateManager implements TerminationListener {

    private static final Map<TscriptVM, JavaAPIStateManager> pool = new HashMap<>();

    public static JavaAPIStateManager getInstance(TscriptVM vm) {
        return pool.computeIfAbsent(vm, k -> new JavaAPIStateManager(vm));
    }


    private final Map<Class<?>, JavaType> types = new HashMap<>();
    private final TscriptVM vm;

    private JavaAPIStateManager(TscriptVM vm) {
        this.vm = vm;
        vm.addTerminationListener(this);
    }

    @Override
    public void onTermination(TscriptVM vm) {
        vm.removeTerminationListener(pool.remove(vm));
    }

    JavaType getType(Class<?> clazz) {
        if (types.containsKey(clazz)) {
            return types.get(clazz);
        }
        JavaType type = new JavaType(clazz, vm);
        types.put(clazz, type);
        type.init();
        return type;
    }

    public void drop(Class<?> clazz) {
        types.remove(clazz);
    }
}
