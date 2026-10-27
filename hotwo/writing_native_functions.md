
# How to write Native Functions

Sometimes it is not possible to do certain tasks in Tscript itself
but only in its underlying implementation language. Functions that do so we call
'native'. Functions that are not native we call 'virtual'.
In Tscript it is possible to call native Functions.<p>

In this Section it is shown how to implement native functions,
by looking at the example of a `greet` function.

Note that common functions like `print` are already built into the VM
and do not need to be declared or registered.


### Step 1: Define the native function in Tscript

The first step is to tell the tscript compiler that a specific
function is native:
```javascript
native function greet;
```

In this implementation of Tscript we do not explicitly give the parameters
(declaring them is a compile error), since they are defined at the native side.

Native functions can also be declared inside classes, for example to implement an abstract method:
```javascript
class A {
    public: native function greet;
}
```


### Step 2: Define the native implementation

Every native implementation is encapsulated in a java class, that inherits
`com.tscript.runtime.tni.NativeFunction`. For example:

```java
package com.example;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.TString;

import java.util.List;

public class NativeGreet extends NativeFunction {

    private static final TString DEFAULT = new TString("world");

    @Override
    public String getName() {
        // the name of the function at runtime.
        // e.g. prints: Function<greet>
        return "greet";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        // one parameter 'name' with a default value
        return Parameters.newInstance().add("name", DEFAULT);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        String name = TNIUtils.toString(env, arguments.get(0));
        if (name == null) return null; // error occurred
        env.getCurrentThread().getVM().getOut().println("hello " + name + "!");
        return Null.INSTANCE;
    }
}
```

Returning `null` from `evaluate` signals that an error occurred. Errors are reported with
`env.reportRuntimeError("message")` (followed by `return null;`) and can be caught
in Tscript like any other error.

The class must have a public no-argument constructor and must be on the classpath of the VM.

### Step 3: Register the Native Implementation

After defining the native implementation we have to register it.
To do so we navigate to the `project.tsrt` file.
Here we add the line `native com.example.NativeGreet`.
Note that the fully qualified name with the dotted notation is required.

### Step 4: Done!

After these three steps you should be able to call your native function
from the tscript side:
```javascript
greet("Tscript"); # prints: hello Tscript!
greet();          # prints: hello world!
```


### Important
The native api purely works <b>by name</b>. This means defining two different
native functions with the same name won't work, not even across included project files.
