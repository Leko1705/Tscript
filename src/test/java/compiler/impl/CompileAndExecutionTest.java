package compiler.impl;

import com.tscript.compiler.tools.*;
import com.tscript.compiler.source.utils.CompileException;
import com.tscript.runtime.core.TscriptVM;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

public class CompileAndExecutionTest {

    public static void main(String[] args) {
        new CompileAndExecutionTest().test();
    }

    @Test
    public void test(){
        compileAndRunAllInDir(
                "src/test/resources",
                "src/test/resources/out",
                "test");
    }

    private void compileAndRunAllInDir(String inPath, String outPath, String bootModule){
        Tool compiler = ToolFactory.createDefaultTscriptCompiler();
        Tool inspector = ToolFactory.loadTool(SupportedTool.TSCRIPT_BC_INSPECTOR);

        removeDir(new File(outPath));

        File[] files = new File(inPath).listFiles();
        if (files == null)
            throw new AssertionError();
        for (File file : files){
            if (!file.getName().endsWith(".tscript")) continue;
            runTool(compiler, file.getAbsolutePath(), outPath + File.separator + file.getName() + "c");
            runTool(inspector, file.getAbsolutePath(), outPath + File.separator + file.getName() + "i");
        }

        TscriptVM vm = TscriptVM.runnableInstance(new File(outPath), System.out, System.err);
        int exitCode = vm.execute(bootModule);
        Assertions.assertEquals(0, exitCode);
    }

    private void removeDir(File dir){
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file: files) {
            if (file.isDirectory())
                removeDir(file);
            file.delete();
        }
    }

    private void runTool(Tool tool, String inPath, String outPath, String... args){
        try {
            InputStream in = new FileInputStream(inPath);
            OutputStream out = new FileOutputStream(outPath);

            try {
                tool.run(in, out, args);
            }
            catch (CompileException e) {
                Assertions.fail(e);
            }
        }
        catch (Exception e){
            Assertions.fail(e);
        }

    }

}
