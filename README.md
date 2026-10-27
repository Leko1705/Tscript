
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

The virtual machine also contains a tracing [JIT compiler](docs/jit_compiler.md),
which makes frequently executed code several times faster than the interpreter.

## Getting Started

Requirements: a JDK (developed and tested with Java 21). Gradle is downloaded by the wrapper.

Set up a project as shown [here](hotwo/project_setup.md). You may use [exampleProject](exampleProject) as
a template.

Then run the project:

#### Using Gradle:

```shell
./gradlew run --args="-p exampleProject/project.tsrt"
```

Relative paths in the project file are resolved against the working directory,
so the example project is run from the repository root.

#### From Java Code:
```java
import com.tscript.projectfile.ProjectFile;
import com.tscript.projectfile.ProjectFileRunner;

public class Main {

    public static void main(String[] args) {
        ProjectFile projectFile = ProjectFile.parse(
                "./exampleProject/project.tsrt");
        int exitCode = ProjectFileRunner.runTscriptProject(projectFile);
        System.exit(exitCode);
    }

}
```

## Documentation

How to:
- [Set up a project](hotwo/project_setup.md)
- [Use the compiler and VM from Java](hotwo/configure_compiler_and_vm.md)
- [Write native functions](hotwo/writing_native_functions.md)
- [Write a debugger](hotwo/writing_debuggers.md)

Internals:
- [Runtime specifications](docs/runtime_specifications.md)
- [Bytecode instruction set](docs/tscript_bytecode_instructions.md)
- [Object model](docs/object_model.md)
- [JIT compiler](docs/jit_compiler.md)

## Frequently Asked Questions

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

This namespace gets compiled like the following class:
```javascript
abstract class math {
    public:

    static const E = 2.718281828459045;

    static function abs(x) {
        if x > 0 then return x;
        else return -x;
    }
}
```

As we can see namespaces are transformed into abstract (non instantiable)
classes with only static members. Therefore `print(math);` prints `Type<math>`.<br>
The same concept of to-Type-conversion also applies to enums, where
the enum constants are static constants in that enum.
