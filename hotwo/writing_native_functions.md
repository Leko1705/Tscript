
# How to write Native Functions

Sometimes it is not possible to do certain tasks in Tscript itself
but its underlying implementation language. Functions that do so we call
'native'. Functions that are not native we call 'virtual'.
In Tscript it is possible to call native Functions.<p>

In this Section it is shown how to implement native functions,
by looking at the example of a print function.


### Step 1: Define the native function in Tscript

The first step is to tell the tscript compiler that a specific
function is native:
```javascript
native function print;
```

In this implementation of Tscript we do not explicitly give the parameters,
since they are defined at the native side.


### Step 2: Define the native implementation

Every native implementation is encapsulated in a java class, that inherits
`com.tscript.runtime.tni.NativeFunction`. For example:

```java
package com.tscript.runtime.tni.natfuncs.std;

import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.tni.TNIUtils;
import com.tscript.runtime.typing.Null;
import com.tscript.runtime.typing.Parameters;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.TString;

import java.util.List;

public class NativePrint extends NativeFunction {
    
    private static final TString DEFAULT = new TString("");

    @Override
    public String getName() {
        // the name of the function at runtime.
        // e.g. prints: Function<print>
        return "print";
    }

    @Override
    public Parameters doGetParameters(Environment env) {
        return Parameters.newInstance().add("text", DEFAULT);
    }

    @Override
    public TObject evaluate(Environment env, List<TObject> arguments) {
        String printString = TNIUtils.toString(env, arguments.get(0));
        if (printString == null) return null; // error occurred
        env.getCurrentThread().getVM().getOut().println(printString);
        return Null.INSTANCE;
    }
}
```

### Step 3: Register the Native Implementation

After defining the native implementation we have to register it.
To do so we navigate to the `project.tsrt` file.
Here we add the line `native com.tscript.runtime.tni.natfuncs.std.NativePrint`.
Note that the fully qualified name with the dotted notation is required.

### Step 4: Done!

After this three steps you should be able to call your native function
from the tscript side:
```javascript
print("working!");
```


### Important
The native api purely works <b>by name</b>. This means defining two different
native functions with the same name won't work.