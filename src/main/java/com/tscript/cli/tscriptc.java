package com.tscript.cli;

import com.tscript.compiler.tools.SupportedTools;
import com.tscript.compiler.tools.Tool;
import com.tscript.compiler.tools.ToolFactory;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@CommandLine.Command(
        name = "tscriptc",
        version = "tscriptc 1.0",
        mixinStandardHelpOptions = true,
        description = "Compiler of the tscript programming language")
public class tscriptc implements Callable<Integer> {

    public static void main(String[] args) {
        new CommandLine(new tscriptc()).execute(args);
    }

    @CommandLine.Option(names = {"-f", "--file"}, description = "input file", paramLabel = "file or directory", required = true)
    File file;

    @CommandLine.Option(names = {"-o", "--output"}, description = "output directory for compilation result")
    public File outputDirectory;

    @CommandLine.Option(names = {"-i", "--inspect"}, description = "sets inspection mode")
    public boolean inspect;

    @CommandLine.Parameters
    public String[] args = {};

    @Override
    public Integer call() throws Exception {

        if (!file.exists()){
            System.err.println("file " + file + " does not exist");
            return -1;
        }

        if (outputDirectory == null){
            outputDirectory = new File(System.getProperty("user.dir"));
        }

        if (!outputDirectory.exists()){
            System.err.println("directory " + file + " does not exist");
            return -1;
        }

        if (!outputDirectory.isDirectory()){
            System.err.println(file + " is not a directory");
            return -1;
        }

        ToolRunner tool = getToolRunner();

        if (file.isDirectory()){
            return compileDirectory(file, outputDirectory, tool, args);
        }
        else {
            return compileFile(file, outputDirectory, tool, args);
        }
    }

    private ToolRunner getToolRunner() {
        ToolRunner tool;
        if (inspect){
            tool = new ToolRunner() {
                @Override
                public void run(InputStream in, OutputStream out, String[] args) {
                    ToolFactory.loadTool(SupportedTools.TSCRIPT_BC_INSPECTOR).run(in, out, args);
                }

                @Override
                public String getOutputExtension() {
                    if (!outputDirectory.isDirectory()) return "";
                    return "i";
                }
            };
        }
        else {
            tool = new ToolRunner() {
                @Override
                public void run(InputStream in, OutputStream out, String[] args) {
                    ToolFactory.createDefaultTscriptCompiler().run(in, out, args);
                }

                @Override
                public String getOutputExtension() {
                    if (!outputDirectory.isDirectory()) return "";
                    return "c";
                }
            };
        }
        return tool;
    }

    private int compileDirectory(File dir, File out, ToolRunner tool, String[] args) {
        try(Stream<Path> stream = Files.walk(dir.toPath())) {
            stream.forEach(path -> compileFile(path.toFile(), out, tool, args));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return -1;
        }
        return 0;
    }

    private int compileFile(File file, File out, ToolRunner tool, String[] args) {
        try {
            String outPath = out.getAbsolutePath() + File.separator + file.getName() + tool.getOutputExtension();
            tool.run(new FileInputStream(file), new FileOutputStream(outPath), args);
            return 0;
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }


    private interface ToolRunner extends Tool {

        void run(InputStream in, OutputStream out, String[] args);

        String getOutputExtension();
    }

}
