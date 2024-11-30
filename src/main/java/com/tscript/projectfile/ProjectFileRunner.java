package com.tscript.projectfile;

import com.tscript.compiler.source.utils.CompileException;
import com.tscript.compiler.tools.SupportedTools;
import com.tscript.compiler.tools.Tool;
import com.tscript.compiler.tools.ToolFactory;
import com.tscript.runtime.VMFactory;
import com.tscript.runtime.core.TscriptVM;
import com.tscript.runtime.debugger.BreakPoint;
import com.tscript.runtime.debugger.Debugger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

public class ProjectFileRunner {

    public static int runTscriptProject(ProjectFile projectFile) {
        return runTscriptProject(projectFile, null, null);
    }

    public static int runDebugTscriptProject(ProjectFile projectFile, Debugger debugger, Set<BreakPoint> breakPoints) {
        Objects.requireNonNull(debugger, "use runTscriptProject(ProjectFile) instead");
        return runTscriptProject(projectFile, debugger, breakPoints);
    }

    private static int runTscriptProject(ProjectFile projectFile, Debugger debugger, Set<BreakPoint> breakPoints) {

        Tool compiler = ToolFactory.createDefaultTscriptCompiler();
        Tool inspector = ToolFactory.loadTool(SupportedTools.TSCRIPT_BC_INSPECTOR);

        try {
            deleteAll(projectFile.getFragmentPath());
            deleteAll(projectFile.getInspectionPath());

            for (String sourcePath : projectFile.getSourcePaths()) {
                Files.walk(Path.of(sourcePath)).forEach(path -> {
                    File file = path.toFile();
                    if (!file.getName().endsWith(".tscript")) return;

                    try {
                        InputStream in = new FileInputStream(file);
                        File outFile = new File(projectFile.getFragmentPath() + File.separator + file.getName() + "c");
                        outFile.getParentFile().mkdirs();
                        outFile.createNewFile();
                        OutputStream out = new FileOutputStream(outFile);
                        compiler.run(in, out, null);

                        if (projectFile.getInspectionPath() != null) {
                            outFile = new File(projectFile.getInspectionPath() + File.separator + file.getName() + "i");
                            outFile.getParentFile().mkdirs();
                            outFile.createNewFile();
                            in = new FileInputStream(file);
                            out = new FileOutputStream(outFile);
                            inspector.run(in, out, null);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        catch (CompileException e){
            throw e;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

        TscriptVM vm = VMFactory.newRunnableTscriptVM(projectFile.getRoots(), System.out, System.err);
        vm.setBuildFile(projectFile);
        if (debugger != null){
            vm.setDebugger(debugger);
            vm.setBreakPoints(breakPoints);
        }
        return vm.execute(projectFile.getBootModule());
    }

    private static void deleteAll(String dir) throws Exception {
        if (dir == null) return;
        Files.list(Path.of(dir)).forEach(path -> {
            File file = path.toFile();
            if (file.isFile())
                file.delete();
        });
    }

}
