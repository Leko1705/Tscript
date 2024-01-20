package runtime.core;

import java.util.HashMap;
import java.util.Map;

public class FileManager {

    private static final FileManager manager = new FileManager();

    private final Map<String, Pool> poolMap = new HashMap<>();

    private FileManager(){}

    public static FileManager getManager() {
        return manager;
    }


    public boolean hasFile(String fileName){
        return poolMap.containsKey(fileName);
    }

    public void putPool(String fileName, Pool pool){
        poolMap.put(fileName, pool);
    }

    public int loadAddress(String fileName, String accessed){
        return 0;
    }

    public Object access(String fileName, int index, TThread thread){
        Pool pool = poolMap.get(fileName);
        return pool.load(index, thread);
    }

}
