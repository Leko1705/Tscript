package com.tscript.runtime.stroage.loading;

import com.tscript.runtime.stroage.Module;

import java.io.File;

public interface ModuleLoader {

    Module loadModule(File[] rootPaths, String[] moduleName) throws ModuleLoadingException;

    Module loadModule(int index) throws ModuleLoadingException;

    int indexOf(String[] moduleName);
}
