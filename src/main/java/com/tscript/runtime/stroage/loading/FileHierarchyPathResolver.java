package com.tscript.runtime.stroage.loading;

import java.io.File;

public class FileHierarchyPathResolver implements FilePathResolver {

    @Override
    public File resolve(File[] rootPaths, String[] moduleName) {
        String extension = FileLoadingUtils.getExtensionString(moduleName);
        for (File rootPath : rootPaths) {
            File candidate = new File(rootPath.getAbsolutePath() + extension);
            if (FileLoadingUtils.checkIfBytecodeFile(candidate, moduleName))
                return candidate;
            candidate = new File(candidate.getAbsolutePath() + ".tscriptc");
            if (FileLoadingUtils.checkIfBytecodeFile(candidate, moduleName))
                return candidate;
        }
        return null;
    }

}
