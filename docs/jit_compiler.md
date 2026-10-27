# Tscript JIT Compiler

Besides the bytecode interpreter, the *Tscript Virtual Machine* contains a
**tracing just-in-time (JIT) compiler**. It records frequently executed paths of a program
("traces"), optimizes them and compiles them to JVM bytecode, which is then
compiled further to machine code by the JVM itself. Hot code therefore runs several
times faster than in the interpreter, while the behaviour of the program (output,
error messages, exit code) stays exactly the same.

The source code lives in the package `com.tscript.runtime.jit`.


## Enabling and Disabling

The JIT is **enabled by default**. It can be turned off

- in the project file with the command `jit off` (`jit on` enables it explicitly),
- from Java code with `vm.setJitEnabled(false)` before the VM is started.

When a debugger is attached, only the interpreter is used.

The JIT is transparent: programs do not have to be changed to benefit from it,
and it never changes the result of a program. If something can not be compiled,
it simply keeps running in the interpreter.


## How it Works

```
 interpreter ──(hot)──> profiler ──> tracer ──> optimizer ──> JVM bytecode emitter ──> compiled image
      ^                                                                                    │
      └──────────────────────────────── deoptimization (guard failed) <────────────────────┘
```

### 1. Profiling

While the interpreter executes a program, the `Profiler` counts

- the calls of every function (separately for every combination of argument types it is called with), and
- the back edges of every loop (jumps back to the loop header).

Every counter walks through the compilation levels (tiers). When a counter
reaches the threshold of the next level, the code is compiled at that level.

| Level | Default threshold | What it does                                                                                                                                       |
|-------|------------------:|----------------------------------------------------------------------------------------------------------------------------------------------------|
| L1    |               100 | Records a trace, runs all optimizations and compiles it.                                                                                           |
| L2    |              1000 | Recompiles the graph of L1 without recording it again and stitches the compiled side traces of its guards into the code (trace trees).             |
| L3    |              3000 | Like L2, but picks up side traces compiled after L2 and stitches deeper. Large graphs are not recompiled if no new side trace can be stitched.     |

All optimizations already run at L1, since long running loops are often entered only
once and never reach L2.

### 2. Tracing

The `Tracer` executes the code like the interpreter does and records the executed instructions
into an intermediate representation (a "sea of nodes" graph). There are three kinds of traces:

- **Function traces** start at the entry of a function and end at its return.
- **Loop traces** start at a hot loop header and end at the back edge.
- **Side traces** start at a guard of another trace that fails often (e.g. the base case of a
  recursive function) and continue until the function returns or a loop header is reached.

Loops inside a trace are represented with phis (one for every local variable and stack slot).
A self tail call (`return f(...)` inside of `f`) closes the loop of the function entry,
so **tail recursion becomes a loop**. Other calls, including non-tail recursion,
dispatch to the compiled code of the callee at runtime. Small functions and methods
are inlined into the trace.

### 3. Speculation and Guards

A trace only covers the path that was recorded. Everything the trace relies on is checked with a
**guard**, for example

- the type of a value (`Integer`, `Real`, `Boolean`, `String`, a class, ...),
- the direction a branch took,
- the function that is called.

Because types are guarded, numbers, booleans and strings can be kept unboxed as
JVM primitives. Every guard remembers the interpreter state at that point (local variables,
operand stack and, for inlined calls, all frames).

### 4. Optimization

Every level runs the following passes until nothing changes any more:

- **Canonicalization** with constant folding and global value numbering
- **Guard elimination**: duplicate guards and guards implied by value ranges are removed
- **Guard hoisting** and loop invariant code motion (in combination with **loop peeling**:
  the first iteration of a loop is recorded in front of the loop)
- **Global promotion**: global variables used in loops are kept in local variables (phis) and
  written back when the loop exits
- **Scalar replacement**: reads of arrays and objects that do not escape the trace are replaced
  by the values written before
- **Allocation removal**: objects that do not escape the trace are never allocated. If a guard
  fails, the object is created from the recorded member values.
- **Object phi splitting**: objects that are carried from one loop iteration to the
  next (`v = v.plus(w)`) are replaced by their members
- **Dead code elimination** (including dead phis)

### 5. Code Generation

The optimized graph is compiled to a JVM class (a `NativeImage`) with the ASM library and loaded
into the running JVM. From then on, the compiled image runs instead of the interpreter.

### 6. Deoptimization

If a guard fails, the image returns a description of the interpreter state at that guard
(including objects that were never allocated and frames of inlined functions). The interpreter
continues exactly there, so the program does not notice the switch. Guards that fail often
get their own side traces; images that fail too often are discarded and the code is recorded again.


## Objects and Classes

Member accesses (`obj.x`, `this.x`, `super.x` and inherited members) are traced. If the object
is an instance of a class, the member is looked up while tracing: guards on the exact type of the
object make the access a read or write at a fixed index. Method calls are inlined like function calls.

Class instantiations are inlined as well, including the constructors and super constructor calls.
Together with allocation removal, short-lived objects (e.g. vectors in numeric code)
often cost nothing at all.


## What is not Compiled

The tracer stops recording (and the code keeps running in the interpreter) when it reaches
one of the following:

- `try`/`catch` blocks and `throw`
- calls with named arguments (`f(x=1)`)
- native functions declared with `native function` (built-in functions like `print` are supported)
- imports, `use` and dynamically looked up names
- class definitions, static members and abstract method lookups
- traces longer than 1500 instructions

Errors raised in compiled code (for example a division by zero or a wrong type) are reported exactly
like in the interpreter, including the error message, the line number and the stack trace.


## Configuration

The JIT can be tuned with JVM system properties (`java -Dtscript.jit.threshold=50 ...`):

| Property                   | Default | Description                                                                              |
|----------------------------|--------:|------------------------------------------------------------------------------------------|
| `tscript.jit.threshold`    |     100 | calls / loop iterations until L1                                                        |
| `tscript.jit.threshold2`   |    1000 | calls / loop iterations until L2                                                        |
| `tscript.jit.threshold3`   |    3000 | calls / loop iterations until L3 (`0` disables L3)                                      |
| `tscript.jit.inlineDepth`  |       3 | maximum depth of nested inlined calls (`0` disables inlining)                           |
| `tscript.jit.inlineSize`   |     120 | maximum number of instructions of an inlined function                                   |
| `tscript.jit.peel`         | `loops` | loop peeling: `loops` (loop traces only), `all` or `none`                                |
| `tscript.jit.background`   | `false` | compile on a background thread instead of the executing thread                          |
| `tscript.jit.l1plain`      | `false` | compile L1 without optimizations                                                        |
| `tscript.jit.debug`        | `false` | print recorded traces, generated JVM bytecode and the reasons of failed traces          |

Further limits are defined in `JITConfiguration`: at most 5 type specializations per function,
an image is discarded after 100 failed guards, an entry is given up after 3 failed attempts,
and compiled images are nested at most 400 levels deep.

The defaults were chosen with the benchmark script (see below). Background compilation is
off by default since it did not pay off in the benchmarks.


## Testing

`src/test/java/runtime/jit/JitDifferentialTest.java` runs every program in
`src/test/resources/jit` once with the interpreter and twice with the JIT (with the default
thresholds and with very low thresholds, so that nearly everything gets traced, compiled and
deoptimized) and checks that the output, error messages and exit code are identical.
`CanonicalizerTest` tests the optimizer on single graphs.

```shell
./gradlew test --tests "runtime.jit.*"
```


## Benchmarks

`scripts/jit-bench.sh` compares the interpreter with the JIT on the programs in
`src/test/resources/bench` and checks that both produce the same output:

```shell
./gradlew compileJava
scripts/jit-bench.sh            # all programs, 3 runs each (median)
scripts/jit-bench.sh -r 5 fib   # single program, 5 runs
scripts/jit-bench.sh -o "-Dtscript.jit.threshold2=500"   # with JIT options
```

Example results (single run, times include the JVM startup):

| Program   | Interpreter | JIT    | Speedup |
|-----------|------------:|-------:|--------:|
| arrays    |    4811 ms  | 496 ms |   9.7x  |
| branchy   |    2625 ms  | 843 ms |   3.1x  |
| calls     |    3265 ms  | 479 ms |   6.8x  |
| collatz   |    3534 ms  | 597 ms |   5.9x  |
| complex   |    5085 ms  | 695 ms |   7.3x  |
| fib       |    2765 ms  | 757 ms |   3.7x  |
| nested    |    1943 ms  | 486 ms |   4.0x  |
| objects   |    2123 ms  | 968 ms |   2.2x  |
| realsum   |    3983 ms  | 527 ms |   7.6x  |
| shapes    |    5455 ms  | 528 ms |  10.3x  |
| strings   |    1271 ms  | 499 ms |   2.5x  |
| tailrec   |    5731 ms  | 745 ms |   7.7x  |
| toplevel  |    1674 ms  | 483 ms |   3.5x  |
| unstable  |    4310 ms  | 438 ms |   9.8x  |
| vectors   |   11723 ms  | 596 ms |  19.7x  |
| whileloop |    2930 ms  | 434 ms |   6.8x  |


## Source Overview

| Package                              | Content                                                                    |
|--------------------------------------|----------------------------------------------------------------------------|
| `jit`                                | `JITCompiler`, configuration, compilation levels                           |
| `jit.tracing`                        | `Profiler`, `Tracer`, `ImageRunner`, deoptimization                        |
| `jit.comp.ir`                        | the graph IR (`Graph`, nodes, types, frame states)                         |
| `jit.comp.optim`                     | the optimization passes                                                    |
| `jit.comp.levels`                    | L1, L2, L3 and the optimizer pipeline                                      |
| `jit.comp.emit`                      | JVM bytecode emitter                                                       |
| `jit.caching`                        | caches of compiled images and objects                                      |
| `jit.schedule`                       | schedulers (same thread / background thread)                               |

A more detailed description of the internals can be found in
`src/main/java/com/tscript/runtime/jit/package-info.java`.
