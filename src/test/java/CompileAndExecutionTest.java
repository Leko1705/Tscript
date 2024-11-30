import com.tscript.projectfile.ProjectFile;
import com.tscript.projectfile.ProjectFileRunner;
import com.tscript.runtime.debugger.BreakPoint;
import com.tscript.runtime.debugger.ConsoleDebugger;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class CompileAndExecutionTest {

    public static void main(String[] args) {
        new CompileAndExecutionTest().test();
    }

    @Test
    public void test(){
        ProjectFile projectFile = ProjectFile.parse(
                "./exampleProject/project.tsrt");
        int exitCode;
        exitCode = ProjectFileRunner.runDebugTscriptProject(projectFile, new ConsoleDebugger(), Set.of(BreakPoint.of("test", 3)));
        //exitCode = ProjectFileRunner.runTscriptProject(projectFile);
        System.exit(exitCode);
    }

}
