package com.tscript.projectfile;

import com.tscript.compiler.source.utils.CompileException;
import com.tscript.compiler.tools.SupportedTools;
import com.tscript.compiler.tools.Tool;
import com.tscript.compiler.tools.ToolFactory;
import com.tscript.runtime.core.TscriptVM;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectFileRunner {

    public static int runTscriptProject(ProjectFile projectFile) {

        Tool compiler = ToolFactory.createDefaultTscriptCompiler();
        Tool inspector = ToolFactory.loadTool(SupportedTools.TSCRIPT_BC_INSPECTOR);

        try {
            Files.walk(Path.of(projectFile.getSourcePath())).forEach(path -> {
                File file = path.toFile();
                if (!file.getName().endsWith(".tscript")) return;

                try {
                    InputStream in = new FileInputStream(file);
                    File outFile = new File(projectFile.getFragmentPath() + File.separator + file.getName() + "c");
                    outFile.getParentFile().mkdirs();
                    outFile.createNewFile();
                    OutputStream out = new FileOutputStream(outFile);
                    compiler.run(in, out, null);

                    if (projectFile.getInspectionPath() != null){
                        outFile = new File(projectFile.getInspectionPath() + File.separator + file.getName() + "i");
                        outFile.getParentFile().mkdirs();
                        outFile.createNewFile();
                        out = new FileOutputStream(outFile);
                        inspector.run(in, out, null);
                    }
                }
                catch (Exception e){
                    throw new RuntimeException(e);
                }
            });
        }
        catch (CompileException e){
            throw e;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

        TscriptVM vm = createTscriptVM(projectFile);
        return vm.execute(projectFile.getBootModule());
    }

    private static TscriptVM createTscriptVM(ProjectFile file) {
        TscriptVM vm = TscriptVM.runnableInstance(file.getRoots(), System.out, System.err);
        vm.setBuildFile(file);
        return vm;
    }

}
