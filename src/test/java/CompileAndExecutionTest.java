import com.tscript.projectfile.ProjectFile;
import com.tscript.projectfile.ProjectFileRunner;
import org.junit.jupiter.api.Test;

public class CompileAndExecutionTest {

    public static void main(String[] args) {
        new CompileAndExecutionTest().test();
    }

    @Test
    public void test(){
        ProjectFile projectFile = ProjectFile.parse(
                "./exampleProject/project.tsrt");
        int exitCode = ProjectFileRunner.runTscriptProject(projectFile);
        System.exit(exitCode);
    }

}
