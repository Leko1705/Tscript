import org.junit.jupiter.api.Test;
import com.tscript.cli.*;

public class CLITest {

    @Test
    void testTscriptCompilerCLI(){
        runTscriptc("-f", "exampleProject/src/test.tscript", "-o", "exampleProject/out");
    }

    @Test
    void testTscriptCompilerCLIInspect(){
        runTscriptc("-f", "exampleProject/src/test.tscript", "-o", "exampleProject/out/inspect", "-i");
    }

    @Test
    void testTscriptRunnerCLI(){
        runTscriptRunner("-m", "test", "-r", "exampleProject/out");
    }

    @Test
    void testTscriptRunnerCLIDebug(){
        runTscriptRunner("-m", "test", "-r", "exampleProject/out", "-d");
    }

    @Test
    void testTscriptRunnerCLIProjectExec(){
        runTscriptRunner("-p", "exampleProject/project.tsrt");
    }

    @Test
    void testTscriptRunnerCLIProjectExecDebug(){
        runTscriptRunner("-p", "exampleProject/project.tsrt", "-d");
    }



    private void runTscriptc(String... args){
        tscriptc.main(args);
    }

    private void runTscriptRunner(String... args){
        tscript.main(args);
    }
}
