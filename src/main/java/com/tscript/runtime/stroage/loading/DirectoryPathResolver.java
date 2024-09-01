package com.tscript.runtime.stroage.loading;

import java.io.File;

public class DirectoryPathResolver implements FilePathResolver {

    private final FilePathResolver parent;

    public DirectoryPathResolver(FilePathResolver parent) {
        this.parent = parent;
    }

    @Override
    public File resolve(File[] rootPaths, String[] moduleName) {
        File file = parent.resolve(rootPaths, moduleName);
        if (file != null) return file;

        for (File root : rootPaths) {
            if (!root.isDirectory()) continue;
            File[] files = root.listFiles();
            if (files == null) continue;
            for (File subFile : files) {
                if (FileLoadingUtils.checkIfBytecodeFile(subFile, moduleName))
                    return subFile;
            }
        }

        return null;
    }
}
