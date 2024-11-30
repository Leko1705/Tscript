package com.tscript.runtime.stroage.loading;

import java.io.File;

public interface FilePathResolver {

    File resolve(File[] rootPaths, String[] moduleName);

}
