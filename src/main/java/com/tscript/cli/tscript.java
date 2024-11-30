package com.tscript.cli;

import com.tscript.projectfile.ProjectFile;
import com.tscript.projectfile.ProjectFileRunner;
import com.tscript.runtime.VMFactory;
import com.tscript.runtime.core.TscriptVM;
import com.tscript.runtime.debugger.ConsoleDebugger;
import picocli.CommandLine;

import java.io.File;
import java.util.HashSet;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "tscript runtime",
        version = "tscript runtime 1.0",
        mixinStandardHelpOptions = true,
        description = "Compiler of the tscript programming language")
public class tscript implements Callable<Integer> {

    public static void main(String[] args) {
        new CommandLine(new tscript()).execute(args);
    }

    @CommandLine.Option(names = {"-p", "--project"}, description = "project file to execute", paramLabel = "project file")
    File projectFile;

    @CommandLine.Option(names = {"-m", "--module"}, description = "module to execute", paramLabel = "module name")
    String bootModule;

    @CommandLine.Option(names = {"-r", "--root"}, description = "root directory", paramLabel = "root directory")
    File root;

    @CommandLine.Option(names = {"-d", "--debug"}, description = "run in debug mode")
    boolean debug;

    @Override
    public Integer call() throws Exception {

        if (projectFile == null && bootModule == null) {
            System.err.println("required either -p or -f option");
            return -1;
        }
        if (projectFile != null && bootModule != null) {
            System.err.println("can not set both options -p and -f");
            return -1;
        }

        if (root == null) {
            root = new File(System.getProperty("user.dir"));
        }

        if (projectFile != null){
            return runProjectFile();
        }
        else {
            return runCustom();
        }
    }

    private int runProjectFile(){
        ProjectFile pf;
        try {
            pf = ProjectFile.parse(projectFile.getAbsolutePath());
        }
        catch (Exception e){
            System.err.println(e.getMessage());
            return -1;
        }

        if (debug){
            return ProjectFileRunner.runDebugTscriptProject(pf, new ConsoleDebugger(), new HashSet<>());
        }
        else {
            return ProjectFileRunner.runTscriptProject(pf);
        }
    }

    private int runCustom(){
        TscriptVM vm = VMFactory.newRunnableTscriptVM(root, System.out, System.err);
        if (debug){
            vm.setDebugger(new ConsoleDebugger());
        }
        return vm.execute(bootModule);
    }
}
