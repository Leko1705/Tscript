
# Tscript Runtime Specifications

The Tscript execution engine is a stack based Virtual Machine (bytecode interpreter with a
just-in-time compiler) and is called the *Tscript Virtual Machine*.
In this section its structure is discussed.


## The Overall Structure

The Tscript Virtual Machine is divided into multiple sections:
![image info](res/vm-structure.png)

Like Java, Tscript supports multithreading by design. This means
that different parts of a program can be executed in parallel.
A `Thread` itself is responsible for managing its own function call stack
via the `Call Stack` and executing the bytecode via the `Bytecode Interpreter`.
There can run multiple threads at a time in the VM.
<p>

All computed data is stored in the heap section. This includes class objects,
but also primitive objects, since everything is an object in this VM implementation.
Since Tscript has automatic memory management, it also requires a `Garbage Collector`
for doing so. For this java implementation the underlying jvm-gc is used.
However, the actual garbage collection algorithm is not specified.
<p>

In order to load and import modules the `Module Loader` is required. 
It loads all module information at runtime if required. Each module itself
is an object and is held by the Module Loader on the Heap.
<p>

When executing native functions, functions implemented in another language (mostly
the underlying VM language, here java) the `Tscript Native Interface` (TNI) can be used 
to do so. It is responsible for loading native implementations as a Tscript
Function Object to the caller. Functions which are non-native are called 'virtual'.
Virtual functions can also be executed from the native side.
<p>

Frequently executed code is compiled by the `JIT Compiler`. While a thread executes bytecode,
a profiler counts function calls and loop iterations. Hot code is recorded as a trace, optimized
and compiled to JVM bytecode. The compiled code then runs instead of the interpreter.
If an assumption of the compiled code does not hold any more, execution continues in the
interpreter at exactly the same point (deoptimization), so the JIT never changes the behaviour
of a program. See [jit_compiler.md](jit_compiler.md) for details.


## Call Stack

The call stack has the following structure:

![image info](res/callstack-structure.png)

A Call Stack consists of multiple `Frames`. Each frame holds information
about a single function call in the stack trace. This information includes:
- <b>Code:</b> the bytecode for this function
- <b>Locals:</b> stores all local variables for a single function call by index
- <b>Operand Stack:</b> stores all currently computed values, as defined in the bytecode.
- <b>Safe Spots:</b> a stack of all instruction addresses to which to jump if an error is thrown,
for example in case of a try-catch statement.
- <b>Names:</b> A map of all names that are used. This map is empty first and might grow over time.
- <b>Execution Registers: </b> holds additional information like:
  - The instruction pointer, pointing to the next executed bytecode instruction
  - the stack pointer, pointing to the top of the operand stack
  - the owner of this function, if existing
  - the module in which this function is defined
  - the current line in the program's source code

Frames are also created for native functions for recreating proper error logs, if printing
the call trace. Of course some frame elements might be absent in that case.

The call stack of a thread is limited to 30,000 frames. Exceeding it raises a stack overflow error.


## Error Handling Procedure

In case of a thrown error the virtual machine performs a backtracking mechanism,
which pops frames of the stack until it has found a top frame whose safe spot stack is not empty.
In that case it pops this address from it, pushes the current error message onto the top
frame and proceeds in that frame at the popped safe spot address.
If the call stack gets empty while popping frames the error is logged to the standard error
and the thread terminates.
Note that <b>any</b> error can be caught.


## Module Structure

All Modules are objects, treated as every other object. However, modules are
created by the Virtual Machine itself and can not be instantiated the common way
(their class is abstract).<p>

Modules consist of multiple parts:
- Constant Pool: Holding constants and names defined in this module only.
- Function Area: Holding Functions meta information for this module only,
from which new virtual Function Objects can be created.
- Type Area: Holding virtual Types with their static initializer functions
for this module only, from which they can be loaded. However, Types are singletons
and never copied, cloned or anything else. When a new Type is loaded which has not been
initialized, its initializer block is called first.


## Bytecode

The bytecode of a module describes its structure:

![image info](res/bytecode-structure.png)

All multi-byte numbers are stored big-endian. Strings are UTF-8 encoded and
end with a null-terminator.

### Header
A compiled file begins with the following header:
- <b>Magic number:</b> the 4 bytes `0x0D 0x0E 0x0A 0x0D` ("DEAD") indicating
  that this may be a valid file containing tscript byte code
- <b>Module name:</b> the name of this module (canonical module path with dot-notation)
- <b>Minor version:</b> the minor version this bytecode supports (1 byte)
- <b>Major version:</b> the major version this bytecode supports (1 byte)
- <b>Entry point:</b> a 2 byte function index of the first function to execute (main function)
- <b>Global variables:</b> a list of names for the global registers, that are important
  for external accesses. Each name is followed by one byte determining whether this register is
  mutable or not. If mutable the byte is 1. The amount of names is determined by a leading 2 byte number.

### Constant Pool
The Constant Pool holds arbitrary constants and information that are
accessed at runtime.<br>
The purpose of the constant pool is to simplify and speed up the
instruction execution process, especially fetching the required data.
The constant pool begins with a 4 byte number encoding the amount of
entries in the pool.
Entries are structured as follows:
<br>
<b>ID | TYPE | VALUE</b><br>
where:<br>
- <b>ID:</b> is a 2 byte number, indexing the entry
- <b>TYPE:</b> a 1 byte tag of the following value
- <b>VALUE:</b> the actual value/constant being stored

The constant pool supports the following types:

| Tag | Type       | Value                                                                                     |
|----:|------------|-------------------------------------------------------------------------------------------|
| 0   | INTEGER    | 4 bytes                                                                                   |
| 1   | REAL       | 8 bytes (IEEE 754 double)                                                                 |
| 2   | STRING     | n bytes + null-terminator                                                                 |
| 3   | BOOL       | 1 byte                                                                                    |
| 4   | NULL       | 0 bytes                                                                                   |
| 5   | RANGE      | 2 * 2 bytes referencing the integer entries of 'from' and 'to'                            |
| 6   | ARRAY      | 1 byte length n, followed by n * 2 bytes referencing any entry                            |
| 7   | DICTIONARY | 1 byte length n, followed by n * 2 * 2 bytes referencing the key and value entries        |
| 11  | UTF8       | n bytes + null-terminator (names used by instructions, e.g. member names)                 |

### Functions
The functions section begins with a 2 byte number encoding the amount
of functions.<br>
Each function's head is encoded as follows:<br>
<b>INDEX | NAME | PARAM_LIST | STACK_SIZE | LOCAL_REGISTER_AMOUNT | INSTRUCTION_AMOUNT</b>
<br>
where:<br>
- <b>INDEX:</b> is the unique 2 byte ID associated with this function. Allows faster lookup for loading a new instance
  (see `LOAD_VIRTUAL`).
- <b>NAME:</b> is the name of the function ending with a null-terminator
- <b>PARAM_LIST:</b> a list of parameters. The amount of parameters is determined by a leading 2 byte number.
  Each parameter is encoded by its name (null-terminated), followed by one byte that is 1 if the parameter
  has a default value and 0 otherwise. If it has a default value, the byte is followed by the 2 byte
  address of the default value in the constant pool.
- <b>STACK_SIZE:</b> a 2 byte number indicating the size of this function's operand stack
- <b>LOCAL_REGISTER_AMOUNT:</b> a 2 byte number indicating the amount of local
  registers for this function (used for local variables)
- <b>INSTRUCTION_AMOUNT:</b> a 4 byte integer encoding the amount of instructions in this function

After the header all its instructions are encoded sequentially.

#### Instructions
Instructions are structured as follows:<br>
<b>OPCODE | ARGS</b><br>
where:<br>
- <b>OPCODE:</b> is a 1 byte number indicating the operation to perform
- <b>ARGS:</b> some optional additional bytes. The amount of the additional bytes depends
  on the OPCODE.

For more information about the actual instructions see [tscript_bytecode_instructions.md](tscript_bytecode_instructions.md).

### Types
The Type section begins with a 2 byte number encoding the amount
of types.<br>
Each type is encoded as follows:<br>
<b>INDEX | NAME | SUPER_TYPE | IS_ABSTRACT | CONSTRUCTOR | STATIC_BLOCK | STATIC_MEMBERS | INSTANCE_MEMBERS</b>
<br>
where:<br>
- <b>INDEX:</b> is the unique 2 byte ID associated with this type. Allows faster lookup for loading.
  This ID is in bounds of: 0 <= ID < amount-of-types, for example the first type might be indexed by 0,
  the second by 1 and so on (see `LOAD_TYPE`).
- <b>NAME:</b> is the name of the type ending with a null-terminator
- <b>SUPER_TYPE:</b> a single byte being 1 if this type has a super type in the same module, followed by
  the 2 byte index of that type. 0 if not (a super type from another module is bound with `BUILD_TYPE`).
- <b>IS_ABSTRACT:</b> a single byte being 1 if this type is abstract
- <b>CONSTRUCTOR:</b> a single byte being 1 if a constructor is provided, followed by the 2 byte index of
  the constructor function and a single byte encoding its visibility (see Type Members). 0 if no
  constructor is provided.
- <b>STATIC_BLOCK:</b> a single byte being 1 if a static member initializer is provided, followed by the
  2 byte index of that function. 0 if no static block is provided.
- <b>STATIC_MEMBERS:</b> a 2 byte number encoding the amount of type bound members, followed by a sequence of
  their actual encoding
- <b>INSTANCE_MEMBERS:</b> a 2 byte number encoding the amount of instance bound members for this type,
  followed by a sequence of their actual encoding

#### Type Members
Type members are structured as follows:<br>
<b>NAME | SPECS</b>
<br>
where:<br>
- <b>NAME:</b> is the name of this member with a null-terminator
- <b>SPECS:</b> a 1 byte integer encoding specific properties for this member.

These are:<br>
Visibility: (Note: if none of the following applies the default visibility is <b>public</b>)
- <b>public:</b> bit 0 (lsb) is 1
- <b>protected:</b> bit 1 is 1
- <b>private:</b> bit 2 is 1

Mutability:<br>
If bit 3 is 1 then this member is mutable, else it is immutable for external accesses (see opcode `STORE_EXTERNAL`).

The other bits are currently not in use. By default, every member is assigned to the null-value.
