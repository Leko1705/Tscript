package com.tscript.buildfile;

import com.tscript.runtime.tni.NativeFunction;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BuildFile {


    public static BuildFile parse(String fileName) {
        try {
            BuildFile buildFile = new BuildFile();

            List<String> lines = Files.readAllLines(Path.of(fileName));

            for (String line : lines) {
                String[] split = line.split(" ");

                if (split[0].equals("include")) {
                    if (split.length != 2) throw new Exception();
                    String included = split[1];
                    Class<?> clazz = Class.forName(included);
                    if (!NativeFunction.class.isAssignableFrom(clazz)) {
                        throw new Exception("included class " + included + " is not a native function");
                    }
                    NativeFunction nativeFunction = (NativeFunction) clazz.getConstructor().newInstance();
                    if (buildFile.natives.containsKey(nativeFunction.getName())) {
                        throw new Exception("native function " + nativeFunction.getName() + " already exists ");
                    }
                    buildFile.natives.put(nativeFunction.getName(), nativeFunction);
                }
                else if (split[0].equals("dependency")) {
                    if (split.length != 2) throw new Exception("missing dependency path");
                    BuildFile file = BuildFile.parse(split[1]);
                    for (Map.Entry<String, NativeFunction> entry : file.natives.entrySet()) {
                        if (buildFile.natives.containsKey(entry.getKey())){
                            throw new Exception("native function " + entry.getKey() + " already exists ");
                        }
                        buildFile.natives.put(entry.getKey(), entry.getValue());
                    }
                }
            }

            return buildFile;
        }
        catch (Exception e) {
            throw new InvalidBuildfileException(e);
        }
    }



    private final Map<String, NativeFunction> natives = new HashMap<>();

    private BuildFile() {}

    public NativeFunction getNative(String name) {
        return natives.get(name);
    }
}
