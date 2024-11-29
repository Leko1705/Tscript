package com.tscript.projectfile;

import com.tscript.runtime.tni.NativeFunction;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ProjectFile {


    public static ProjectFile parse(String filePath){
        return parse(filePath, new HashSet<>());
    }

    private static ProjectFile parse(String fileName, Set<File> loaded) {
        int lineNum = 0;
        try {
            ProjectFile projectFile = new ProjectFile();

            List<String> lines = Files.readAllLines(Path.of(fileName));

            for (String line : lines) {
                lineNum++;
                String[] split = line.split(" ");

                switch (split[0]) {
                    case "native" -> {
                        if (split.length != 2) throw new Exception();
                        String included = split[1];
                        Class<?> clazz = Class.forName(included);
                        if (!NativeFunction.class.isAssignableFrom(clazz)) {
                            throw new Exception("included class " + included + " is not a native function");
                        }
                        NativeFunction nativeFunction = (NativeFunction) clazz.getConstructor().newInstance();
                        if (projectFile.natives.containsKey(nativeFunction.getName())) {
                            throw new Exception("native function '" + nativeFunction.getName() + "' already loaded");
                        }
                        projectFile.natives.put(nativeFunction.getName(), nativeFunction);
                    }
                    case "package" -> {
                        if (split.length != 2) throw new Exception("missing dependency path");
                        String dirName = split[1];
                        Files.walk(Path.of(dirName)).forEach(path -> {
                            File file = path.toFile();
                            if (file.getAbsolutePath().endsWith(".tsrt")) {
                                if (loaded.contains(file)) return;
                                loaded.add(file);
                                ProjectFile bf = ProjectFile.parse(file.getAbsolutePath(), loaded);
                                try {
                                    bind(bf, projectFile);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            else if (file.getAbsolutePath().endsWith(".tscript")) {
                                projectFile.sourcePaths.add(file.toPath().getParent().toAbsolutePath().toString());
                            }

                            else if (file.getAbsolutePath().endsWith(".tscriptc")) {
                                projectFile.roots.add(file.toPath().getParent().toAbsolutePath().toFile());
                            }
                        });
                    }
                    case "src" -> {
                        if (split.length != 2) throw new Exception();
                        if (projectFile.sourcePaths.contains(split[1])) throw new Exception("source path already set");
                        projectFile.sourcePaths.add(split[1]);
                    }
                    case "out" -> {
                        if (split.length != 2) throw new Exception();
                        if (projectFile.fragmentPath != null) throw new Exception("output path already set");
                        projectFile.fragmentPath = split[1];
                    }
                    case "inspection" -> {
                        if (split.length != 2) throw new Exception();
                        if (projectFile.inspectionPath != null) throw new Exception("inspection path already set");
                        projectFile.inspectionPath = split[1];
                    }
                    case "run" -> {
                        if (split.length != 2) throw new Exception();
                        if (projectFile.bootModule != null) throw new Exception("boot module already set");
                        projectFile.bootModule = split[1];
                    }
                    case "root" -> {
                        if (split.length != 2) throw new Exception();
                        File root = new File(split[1]);
                        projectFile.roots.add(root);
                    }
                }

            }

            if (projectFile.fragmentPath != null) {
                projectFile.roots.add(new File(projectFile.fragmentPath));
            }

            return projectFile;
        }
        catch (Exception e) {
            throw new InvalidProjectFileException("In line " + lineNum + ": " + e.getMessage(), e);
        }
    }

    private static void bind(ProjectFile merged, ProjectFile actual) throws Exception {
        for (Map.Entry<String, NativeFunction> entry : merged.natives.entrySet()) {
            if (actual.natives.containsKey(entry.getKey())){
                throw new Exception("native function '" + entry.getKey() + "' already loaded");
            }
            actual.natives.put(entry.getKey(), entry.getValue());
        }
    }



    private final Map<String, NativeFunction> natives = new HashMap<>();

    private final Set<String> sourcePaths = new HashSet<>();

    private String fragmentPath;

    private String inspectionPath;

    private final Set<File> roots = new HashSet<>();

    private String bootModule;

    private ProjectFile() {}

    public NativeFunction getNative(String name) {
        return natives.get(name);
    }

    public File[] getRoots() {
        return roots.toArray(new File[0]);
    }

    public String getBootModule() {
        return bootModule;
    }

    public Set<String> getSourcePaths() {
        return sourcePaths;
    }

    public String getFragmentPath() {
        return fragmentPath;
    }

    public String getInspectionPath() {
        return inspectionPath;
    }
}
