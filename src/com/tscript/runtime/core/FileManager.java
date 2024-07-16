package com.tscript.runtime.core;

import com.tscript.runtime.type.Callable;
import com.tscript.runtime.type.TType;

import java.io.File;
import java.util.*;

public class FileManager {

    private static final FileManager manager = new FileManager();

    private final Map<String, Pool> pools = new HashMap<>();

    private final Map<String, Collection<TType>> types = new HashMap<>();

    private FileManager(){}

    public static FileManager getManager() {
        return manager;
    }


    public boolean hasFile(String fileName){
        return pools.containsKey(fileName);
    }

    public void loadFile(String path, TscriptVM vm, TThread thread) {
        FileLoader loader = new FileLoader(new File(path), vm, thread);
        loader.load();
        Pool pool = loader.getPool();
        pools.put(path, pool);

        List<TType> typesInFile = new ArrayList<>();
        for (Pool.Entry<?> entry : pool)
            if (entry instanceof Pool.Type)
                typesInFile.add(((Pool.Type) entry).load());
        types.put(path, typesInFile);
    }

    public int loadAddress(String path, String accessed){
        Pool pool = pools.get(path);
        int i = 0;
        for (Pool.Entry<?> entry : pool) {
            if (accessed.equals(entry.toString()))
                return i;
            i++;
        }
        return -1;
    }

    public Object access(String path, int index, TThread thread){
        Pool pool = pools.get(path);
        return pool.load(index, thread);
    }

    public Map<String, Collection<TType>> getTypes(){
        return types;
    }


}
