package runtime.jit.table;

import runtime.type.Callable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class LookUpTableImpl implements LookUpTable {

    private final Map<String, Callable> table = new ConcurrentHashMap<>();

    @Override
    public boolean isOptimized(Callable callable) {
        Objects.requireNonNull(callable);
        return table.containsKey(callable.getName());
    }

    @Override
    public Callable getOptimized(Callable callable) {
        Objects.requireNonNull(callable);
        return table.get(callable.getName());
    }

    @Override
    public void addOptimized(Callable callable, Callable optimized) {
        if (table.containsKey(callable.getName())) return;
        Objects.requireNonNull(callable);
        Objects.requireNonNull(optimized);
        table.put(callable.getName(), optimized);
    }

    @Override
    public void removeOptimized(Callable callable) {
        Objects.requireNonNull(callable);
        table.remove(callable.getName());
    }

    @Override
    public void replaceOptimized(Callable callable, Callable optimized) {
        Objects.requireNonNull(callable);
        Objects.requireNonNull(optimized);
        table.put(callable.getName(), optimized);
    }

}
