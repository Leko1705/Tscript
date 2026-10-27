
# How to write a Debugger

In this section it is shown how one can write and attach their own custom debugger.

### Step 1: Define the Debugger

Implement your Debugger:
```java
import com.tscript.runtime.debugger.Debugger;
import com.tscript.runtime.debugger.DebugActionObserver;
import com.tscript.runtime.debugger.states.VMState;

public class MyDebugger implements Debugger {

    @Override
    public void onHalt(long threadId, VMState state, int line, DebugActionObserver observer){
        // implement debugger logic here

        // notify the observer to continue the execution
        observer.notify(Action.RESUME);
    }

}
```

Alternatively extend the wrapper class implementation:
```java
import com.tscript.runtime.debugger.WrappedDebugger;
import com.tscript.runtime.debugger.states.VMState;

public class MyDebugger extends WrappedDebugger {

    @Override
    public void onHalt(long threadId, VMState state, int line){
        // implement debugger logic here

        // notify the observer to continue the execution
        notify(Action.RESUME);
    }

}
```

The available actions are `STEP_OVER`, `STEP_OUT`, `RESUME` and `QUIT`.
The `VMState` gives access to the threads (with their frames, locals and operand stacks)
and the loaded modules at the moment the VM was halted.

<b>Important:</b> The debugger expects the observer to be notified.
If not done the program is locked and will <b>not</b> continue! An observer
can not be notified twice in one debug-halt process.

A ready-to-use command line debugger is provided by `com.tscript.runtime.debugger.ConsoleDebugger`.


### Step 2: Attach the Debugger

To run the Virtual Machine with your custom debugger you have to attach it
<b>before</b> the VM is started:
```java
TscriptVM vm = ...;
vm.setDebugger(new MyDebugger());
```

While a debugger is attached, the code is executed by the bytecode interpreter only;
the [JIT compiler](../docs/jit_compiler.md) does not compile anything.


### Step 3: Modify Breakpoints

To modify the breakpoints simply use:

```java
TscriptVM vm = ...;
Set<BreakPoint> breakPoints = vm.getBreakPoints();
breakPoints.add(BreakPoint.of("moduleName", 12)); // halt at line 12 of module 'moduleName'
```

or replace the whole set with `vm.setBreakPoints(...)`.


### Alternative: Using Project Files

In order to attach a debugger to a running project use:
```java
ProjectFileRunner.runDebugTscriptProject(projectFile, new MyDebugger(), new HashSet<>());
```

From the command line, a project can be run with the `ConsoleDebugger` attached
by passing the `-d` flag:
```shell
tscript -p project.tsrt -d
```
