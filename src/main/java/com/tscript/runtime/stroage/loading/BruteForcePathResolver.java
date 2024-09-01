package com.tscript.runtime.stroage.loading;

import java.io.File;

public class BruteForcePathResolver implements FilePathResolver {

    private final FilePathResolver parent;

    public BruteForcePathResolver(FilePathResolver parent) {
        this.parent = parent;
    }

    @Override
    public File resolve(File[] rootPaths, String[] moduleName) {
        File result = parent.resolve(rootPaths, moduleName);
        if (result != null) return  result;
        return searchRecursive(rootPaths, moduleName);
    }

    private static File searchRecursive(File[] toCheck, String[] moduleName) {
        for (File rootPath : toCheck) {
            if (rootPath.isDirectory()) {
                File[] files = rootPath.listFiles();
                if (files == null) continue;
                File candidate = searchRecursive(files, moduleName);
                if (candidate != null) return candidate;
            }
            else {
                if (FileLoadingUtils.checkIfBytecodeFile(rootPath, moduleName))
                    return rootPath;
            }
        }

        return null;
    }
}
