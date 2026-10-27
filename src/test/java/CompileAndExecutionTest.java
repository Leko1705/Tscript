import com.tscript.projectfile.ProjectFile;
import com.tscript.projectfile.ProjectFileRunner;
import org.junit.jupiter.api.Test;

public class CompileAndExecutionTest {

    public static void main(String[] args) {
        new CompileAndExecutionTest().test();
    }

    @Test
    public void test(){
        long startTime = System.currentTimeMillis();
        ProjectFile projectFile = ProjectFile.parse(
                "./exampleProject/project.tsrt");
        int exitCode;
        //exitCode = ProjectFileRunner.runDebugTscriptProject(projectFile, new ConsoleDebugger(), Set.of(BreakPoint.of("test", 3)));
        exitCode = ProjectFileRunner.runTscriptProject(projectFile);
        System.out.println("Finished in " + (System.currentTimeMillis() - startTime) + "ms");
        System.exit(exitCode);
    }

}
