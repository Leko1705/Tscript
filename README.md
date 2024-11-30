
# Welcome to Tscript!

(Teaching Script) Tscript is a dynamically typed programming language,
designed for teaching common programming concepts to newcomers and 
advanced programmers. This implementation is a re-implementation 
of the original implementation which can be found
<a href='https://github.com/TGlas/tscript'>here</a>.
Besides the original features this implementation supports a couple
of new features or feature changes.

## What's new?
A list of all new features can be found [here](whats%20new.md).

## Getting Stated

Set up a project as shown [here](/hotwo/project_setup.md). You may use [exampleProject](exampleProject) as
a template.

Then run the project:

#### Using Docker:

```shell
docker build -t tscript .
docker run -p 8080:8080 tscript
```

#### From Java Code:
```java
import com.tscript.projectfile.ProjectFile;
import com.tscript.projectfile.ProjectFileRunner;

import java.util.Set;

public class Main {

    public static void main(String[] args) {
        ProjectFile projectFile = ProjectFile.parse(
                "./exampleProject/project.tsrt");
        int exitCode = ProjectFileRunner.runTscriptProject(projectFile);
        System.exit(exitCode);
    }

}
```

## Often Asked Questions

### Why are namespaces treated as Types?

Namespaces are generated as classes.
Consider the following namespace:

```javascript
namespace math {
    const E = 2.718281828459045;
    
    function abs(x) {
        if x > 0 then return x;
        else return -x;
    }
}
```

This namespace gets compiled as:
```javascript
abstract class math {
    public:
        
    static const E = 2.718281828459045;
    
    static function abs(x) {
        if x > 0 then return x;
        else return -x;
    }
    
    private:
    constructor(){
        throw "namespaces can not get instanted";
    }
}
```

As we can see namespaces are transformed into non instantiable or inheritable
classes with only static members.<br>
The same concept of to-Type-conversion also applies to enums, where
the enums constants are static constants in that enum.
