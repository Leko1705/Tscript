
# How to write Native Functions

In this section it is shown how one can write and attach his own custom debugger.

### Step 1: Define the Debugger

Implement your Debugger:
```java
import com.tscript.runtime.debugger.Debugger;
import com.tscript.runtime.debugger.DebugActionObserver;

public class MyDebugger implements Debugger {

    void onHalt(long threadId, VMState state, DebugActionObserver observer){
        // implement debugger logic here
        
        // notify the observer to continue the execution
        observer.onAction(Action.RESUME);
    }
    
}
```

Alternatively extend the Wrapper class implementation:
```java
import com.tscript.runtime.debugger.Debugger;
import com.tscript.runtime.debugger.DebugActionObserver;

public class MyDebugger extends WrappedDebugger {

    public abstract void onHalt(long threadId, VMState state){
        // implement debugger logic here
        
        // notify the observer to continue the execution
        notify(Action.RESUME);
    }
    
}
```

<b>Important:</b> The debugger expects the observer to be notified.
If not done the program is locked and will <b>not</b> continue!


### Step 2: Attach the Debugger

To run the Virtual Machine with you custom debugger you have to attach it.
You can do so by calling:
```java
TscriptVM vm = ...;
vm.setDebugger(new MyDebugger());
```


### Step 3: Modify Breakpoints

To modify breakpoint simply use:

```java
TscriptVM vm = ...;
Set<Integer> breakpoints = vm.getBreakPoints();
// modify the breakpoint set
```


### Alternative: Using Project Files

In order to attach a debugger to a running project use:
```java
import java.util.HashSet;ProjectFileRunner.runDebugTscriptProject(projectFile, new MyDebugger(), new HashSet<>());
```