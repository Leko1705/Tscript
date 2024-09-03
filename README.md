# The Tscript Language Specification
This is the specification for the *Tscript Programming Language*, 
especially for its compiler and its runtime environment, also called the 
*Tscript Virtual Machine*. It specifies the properties
of the original Tscript implementation, which can be found
<a href='https://github.com/TGlas/tscript'>here</a>, for a bytecode compiled
and executed version, except for a few changes:<p>

#### Dictionaries
Dictionaries now allow other key types than `String`, for example:
```javascript
var x = {1: true, null: "text"};
print(x[null]); # prints: text
```

#### typeof keyword
The new Tscript version now supports the `typeof` keyword, which can be used
in two scenarios:<br>
For type checking:
```javascript
if (x typeof String){
    # x is a string
}
```
which is equivalent to:
```javascript
if (Type(x) == String){
    # x is a string
}
```
<p>

Or for accessing the type:
```javascript
var type = typeof x;
```
which is equivalent to:
```javascript
var type = Type(x);
```

#### overridden keyword

The `overridden` keyword tells the compiler that this is an overridden method.
It throws a compile time error if the method does not override a super method.
```javascript
class A {
    public: function foo(){
        print("a");
  }
}

class B {
    public: overridden function foo(){
        print("b");
  }
}
```

The `overridden` keyword is optional and should improve the codes' readability.
The keyword is not applicable to constructors or static functions.

#### abstract Types

Types now can be declared as abstract, preventing instantiations of that Type directly.
```javascript
abstract class A {
    public: constructor(){
        ...
    }
}

class B: A {
    public: constructor(): super(){
        ...
    }
}

var b = B(); # OK
var a = A(); # ERROR
```

As part of this abstract methods are also supported.
```javascript
abstract class A {
    public: function foo(){
        bar();
    }
    public: abstract function bar();
}

class B: A {
    public: overridden function bar(){
        print("hello world!");
    }
}

var b = B();
b.foo(); # prints: hello world!
```

Abstract methods can only be defined in abstract Types.
They also can be overridden via native functions.

```javascript
class C: A {
    public: native overridden function bar(); # overrides abstract function bar in A
}
```

#### Native Functions

With the new Tscript implementation the `native` keyword is
introduced. It allows to define functions that are implemented
and executed from the underlying execution engine. For example:<br>
example.tscript:
```javascript
native function print(x="");

print("hello world!"); # prints: hello world to the standard-output
```

print.c:
```java
BaseObject* print(BaseObject* obj){
    char* asString = obj_to_str(obj);
    puts(asString);
    return NULL;
}
```

Native Functions can also be defined in classes.
```javascript
class A {
    public: native function print(x="");
}
```

#### for-loop manipulation
if a `for` loop is used it is possible to manipulate the index
when iterating over a `Range`. However it must be guaranteed that
the iterated object is a `Range`!

```javascript
for var i in 0:10 {
    if i == 2 then {
        i = 9;
        continue;
    }
    print(i);
}

# prints: 0, 1
```

```javascript

function foo(range) {
    
    for var i in range {
        if i == 2 then
        {
            i = 9;
            continue;
        }
        print(i);
    }

    # prints: 0, 1, 3, 4, 5, 6, 7, 8, 9
    # since range is not guaranteed to 
    #be type of Range
}

foo(0:10);
```

The second example always happens if the iterated object is
either not type of range, or a variable.

#### const keyword
In Tscript the `const` keyword is now supported.
It prevents variables from getting reassigned.
Every constant must be initialized immediately.

```javascript
var x = 1;
x = 2; # OK

const y = 2;
y = 3; # ERROR

const z; # ERROR constant is not initialized
```

Constants that are defined in classes are checked at runtime.
```javascript
class A {
    public: const x = 3;
}

var a = A();
a.x = 4; # ERROR at runtime
```

#### imports
Tscript now supports the `import` keyword, allowing the usage of other files.<p>
utils.tscript
```javascript
native function print(x="");
```
main.tscript
```javascript
import utils;

utils.print("hi");
```

You also may use the keyword `from` to specify your import.
```javascript
from utils import print;

print("hi");
```

To import everything with `from` use `from utils import *`.<br>
When using `import` the imported module is executed and all its
global definitions are then available, even variables and constants.

#### custom main function

If you do want to have a specified main function you can simply define your own.
```javascript
function __main__(){
    print("hi");
}
```
This is equivalent to
```javascript
print("hi");
```
Keep in mind that there can only be one main function per module.

## The Tscript Compiler

The Tscript compiler is a program that takes in a 
Tscript source code file and compiles it into a different
Format. In most cases this format is the Tscript Bytecode
which then can be executed by the *Tscript Virtual Machine*.

### Agenda

- How to use
  - bash
  - java
  - extension Plugins
- Phases
  - Syntactic Analysis
      - Lexer
      - Parser
  - Semantic Analysis
    - Definition Checker
    - Scope Checker
    - Type Checker
  - Generation
- Tscript Bytecode Structure
  - Meta data
  - Constant pool
  - Functions
  - Types
  - Instructions
  - Instruction Set

### How to use
#### bash
The Tscript compiler can be used from the command line.<br>
In order to compile the Tscript code simply run.
```bash
tscriptc myFile.tscript
```
You can also use  ```-o output-diectory``` to specify 
the output location of the compiled file, like:
```bash
tscriptc myFile.tscript -o my/wanted/output_directory
```
By default, the output location is the location in which the source code 
file lays. <p>
In order to select the target just add ```-t target_type```, like:
```bash
tscriptc myFile.tscript -t python
```
By default, the target is the Tscript Bytecode.
#### java
Since the Tscript compiler is written in java it is possible to
access the compiler from there and run it.<br>
To Archive this you might use:

```java
import java.io.*;
import com.tscript.tscriptc.tools.Compiler;
import com.tscript.tscriptc.tools.CompilerProvider;

public static void main(String[] args) {
    InputStream in = new FileInputStream("code.tscript");
    OutputStream out = new FileOutputStream("code.tscriptc");
    
    Compiler compiler = CompilerProvider.getDefaultTscriptCompiler();
    int exitCode = compiler.run(in, out, null, null);
    
    System.exit(exitCode);
}
```

#### extension Plugins
To make the Compiler more extensible for future changes the Compiler
Architecture allows plugins. Plugins allow to access the compiler logic.
<br>
Compilers can be used as follows:

```java
import com.tscript.tscriptc.plugin.Plugin;
import com.tscript.tscriptc.tools.Compiler;
import com.tscript.tscriptc.tools.CompilerBuilder;
import com.tscript.tscriptc.tools.Language;
import com.tscript.tscriptc.utils.Phase;

public static void main(String[] args) {
    Compiler compiler = new CompilerBuilder(Language.TSCRIPT)
            .addPlugin((event) -> {
                if (event.getPhase() == Phase.CHECKING){
                    // perform some operation while checking phase
                }
            }).build();
}
```

### Phases
The Tscript Compiler Workflow can be broken down
into three Phases. Each Phase is executed only if
the previous Phase has finished completely.

#### 1) Syntactic Analysis
In the first Phase the syntax of the given code is analysed.
If the given code is syntactic incorrect a SyntaxError is thrown
and logged with the used logger. In this situation the compiler
returns -1 for the exit code.

##### Lexer
In the syntactic phase a Lexer scans the raw source code and collects 
all tokens from the file. A Token represents a group of characters or a word 
(called lexeme) that is associated with a specified meaning. <br>
For example the word 'function' will get identified as a 
function-keyword. Each Token contains its lexeme, the tag which
represents its meaning and the location of where the token was identified.
The Lexer works with the peek-consume-principle and is used by the Parser.

##### Parser
The Parser is responsible for bringing the tokens provides by the lexer
into a Tree-Structure which represents the syntactic structure of the program.
In order to decouple algorithms operating on the Tree the Visitor pattern is used.
<p>
The Tscript parser is a LL(1) recursive-decent-parser.<br>
<p>
After the parsing process has finished the syntactic analysis has finished.

#### 2) Semantic Analysis
After the syntactic analysis has completed the semantic analysis follows.
Here the structured tokens are analyzed for information and contradictions that
are not detected yet. This includes:

##### Definition Checker
Checks if each used variable is defined. The result 
of this check is a dependency graph, holding all dependencies, definitions, references
and inheritance information.

##### Scope Checker
Checks for thinks like invalid return, break or continue usages.

##### Type Checker
Performs simple type checking for invalid type usages that can be
determined at compile time, such as expressions like `1 + null` or
calling a non-callable e.g. `1()`.

#### 3) Generation
In the last Phase the actual lower code is generated.
For this the generator uses the Dependency Graph, built
by the Definition Checker and a Target.
A Target defines the format of the output file. For example a 
Target could be the Tscript Bytecode or a python file for transpiling.

### Tscript Bytecode Structure
The Tscript Bytecode represents a low level bytecode which can be
read and executed by the *Tscript Virtual Machine*. It provides the
following structure from file-start to file-end:

#### Meta data
A compiled files begins with the following metadata:
- <b>Magic number:</b> the number `0xDEAD` in 2 bytes indicating
that this may be a valid file containing tscript byte code
- <b>module name:</b> the name of this module (canonical module path with dot-notation)
- <b>Minor version:</b> the minor version this bytecode supports (1 byte)
- <b>Major version:</b> the major version this bytecode supports (1 byte)
- <b>Entry point:</b> a 2 byte function id for the first function to execute (main function)
- <b>Global variables:</b> a list of names for the global registers, that are important
for external accesses. Each name is followed by one byte determining weather this register is
mutable or not. If mutable the byte is 1. The amount of names is determined by a leading 2 byte number.

#### Constant Pool
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
- <b>ID:</b> is a 2 byte size number, indexing the entry
- <b>TYPE:</b> the type of the following value
- <b>VALUE:</b> the actual value/constant being stored

The constant pool supports the following types:
- INTEGER (4 bytes)
- REAL (8 bytes)
- STRING (n bytes + null-terminator)
- BOOL (1 byte)
- NULL (0 byte)
- RANGE (2 * 2 bytes referencing to integer values in the constant-pool)
- ARRAY (n * 2 bytes referencing to any entry in the constant pool + 1 byte array length representing n)
- DICTIONARY (n * 2 * 2 bytes for key and value, each referencing any entry 
in the constant pool + 1 byte dictionary length regarding the entries representing n)
- UTF8 (n bytes + null-terminator)

#### Functions
The functions section begins with a 2 byte number encoding the amount
of functions.<br>
Each functions head is encoded as follows:<br>
<b>INDEX | NAME | PARAM_LIST | STACK_SIZE | LOCAL_REGISTER_AMOUNT | INSTRUCTION_AMOUNT</b>
<br>
where:<br>
- <b>INDEX:</b> is the unique 2 byte ID associated with this function. Allows faster lookup for loading a new instance.
The index is stored in the constant pool for the VFUNCTION entry.
- <b>NAME:</b> is the name of the function ending with a null-terminator
- <b>PARAM_LIST:</b> a list of parameters. Each parameter is encoded by starting with its name,
the end is determined with a null-terminator, and its 2 byte default-value-address,
referencing to a constant in the constant pool. If a parameter has no default value its index-byte is -1.
The amount of parameters is determined with a leading byte.
- <b>STACK_SIZE:</b> a 2 byte number indicating the size of this functions stack
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

#### Types
The Type section begins with a 2 byte number encoding the amount
of types.<br>
Each type is encoded as follows:<br>
<b>INDEX | NAME | SUPER_TYPE_INDEX | IS_ABSTRACT | CONSTRUCTOR_INDEX | STATIC_BLOCK_INDEX |STATIC_MEMBERS </b>
<br>
where:<br>
- <b>INDEX:</b> is the unique 2 byte ID associated with this type. Allows faster lookup for loading.
This ID is in bounds of: 0 <= ID < amount-of-types, for example the first type might be indexed by 0,
the second by 1 and so on.
The index is stored in the constant pool for the TYPE entry
- <b>NAME:</b> is the name of the type ending with a null-terminator
- <b>SUPER_TYPE_INDEX:</b> a 2 byte ID referencing a function-entry in the constant pool.
Is -1 1 if this class has no super type
- <b>IS_ABSTRACT:</b> a single byte being 1 if this type is abstract
- <b>CONSTRUCTOR_INDEX:</b> a 2 byte index referencing a function that will be called as the constructor.
  Is -1 if no constructor is provided
- <b>STATIC_BLOCK_INDEX:</b> a 2 byte index referencing a function that will be called as the static member initializer.
  Returns -1 if no static block is provided
- <b>STATIC_MEMBERS:</b> a 2 byte number encoding the amount of type bound members, followed by a sequence of
their actual encoding
- <b>INSTANCE_MEMBER:</b> a 2 byte number encoding the amount of instance bound members for this type, 
followed by a sequence of their actual encoding

#### Type members
Type members are structured as follows:<br>
<b>NAME | SPECS</b>
<br>
where:<br>
- <b>NAME:</b> is the name of this member with a null-terminator
- <b>SPECS:</b> a 1 byte integer encoding specific properties for this member.

These are:<br>
Visibility: (Note: if no of the following applies the default visibility is <b>public</b>)
- <b>public:</b> lsb is 1
- <b>protected:</b> second bit after lsb is 1
- <b>private:</b> third bit after lsb is 1

Mutability:<br>
If the forth bit after lsb is 1 then this member is immutable for external accesses (see opcode `STORE_EXTERNAL`), 
else mutable.

The other bits are currently not in use. By default, every member is assigned to the null-value.

#### Instruction Set
<b>Note:</b> `op-stack` stands for `operand-stack` (see more in the Virtual machine specifications). The most left value is on top of the stack.

| Opcode |      Name       | args (in bytes) |                                                                                                                                                                                                                                                                                                                                                                           Description |                                                           Stack Operation (before -> after) |
|--------|:---------------:|----------------:|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|--------------------------------------------------------------------------------------------:|
| 0      |    PUSH_NULL    |               0 |                                                                                                                                                                                                                                                                                                                                            Pushes the `null` value onto the op-stack. |                                                                              `[] -> [null]` |
| 1      |    PUSH_INT     |               1 |                                                                                                                                                                                                                                                                                                                                 Pushes an integer -128 <= x <= 127 onto the op-stack. |                                                                          `[] -> [some_int]` |
| 2      |    PUSH_BOOL    |               1 |                                                                                                                                                                                                                                                                                                             Pushes the boolean `true` onto the op-stack if the arg is 1 else `false`. |                                                                         `[] -> [some_bool]` |
| 3      |   LOAD_CONST    |               2 |                                                                                                                                                                                                  Loads the constant from the constant pool indexed by the 2 byte arguments. A constant is one of the following types: Integer, Boolean, Real, String, Null, Range, Array, Dictionary. |                                                                                 `[] -> [c]` |
| 4      |    LOAD_TYPE    |               2 |                                                                                                                                                                                                                                      Loads a type onto the op-stack, that is stored in the bytecode. The function is loaded by the given 2 byte index (see more in the type section). |                                                                              `[] -> [type]` |
| 6      |    PUSH_THIS    |               0 |                                                                                                                                                                                                                                                                                                                                                      Pushes `this` onto the op-stack. |                                                                              `[] -> [this]` |
| 4      |   LOAD_NATIVE   |               2 |                                                                                                                                                                      Loads a native function onto the op-stack. The function is loaded by name. The name is loaded from the constant pool by its 2 byte address. Throws an error if the native function for this name does not exist. |                                                                   `[] -> [native_function]` |
| 4      |  LOAD_VIRTUAL   |               2 |                                                                                                                                                                                                                                                     Loads a virtual function onto the op-stack. The function is loaded by the given 2 byte index (see more in the functions section). |                                                                  `[] -> [virtual_function]` |
| 4      |    SET_OWNER    |               0 |                                                                                                                          Removes a function as the top element from the op-stack then removes and assigns the new top element as the owner to that function. The modified function is then pushed back onto the op-stack. Throws an error if the first top element is not a Function. |                                                           `[function, owner] -> [function]` |
| 7      |       POP       |               0 |                                                                                                                                                                                                                                                                                                                                            Removes the top element from the op-stack. |                                                                               `[top] -> []` |
| 8      |    NEW_LINE     |               4 |                                                                                                                                                                                                                                                                                           Marks that a new line is reached. The line is encoded as a 4 byte integer by the arguments. |                                                                                  `[] -> []` |
| 9      |   LOAD_GLOBAL   |               1 |                                                                                                                                                                                                                                                                                                                                      Loads an element by index from the global scope. |                                                                       `[] -> [some_global]` |
| 10     |  STORE_GLOBAL   |               1 |                                                                                                                                                                                                                                                          Removes the top element from the op-stack and stores it by index into the global scope. The registers mutability is ignored. |                                                                       `[some_global] -> []` |
| 11     |   LOAD_LOCAL    |               1 |                                                                                                                                                                                                                                                                              Removes the top element from the op-stack and stores it into the local register indexed by the argument. |                                                                        `[] -> [some_local]` |      
| 12     |   STORE_LOCAL   |               1 |                                                                                                                                                                                                                                                                              Removes the top element from the op-stack and stores it into the local register indexed by the argument. |                                                                        `[some_local] -> []` |
| 13     |  LOAD_EXTERNAL  |               2 |     Performs an external member read operation by removing the accessed element from the top of the op-stack and accesses the member my its name. The name is accessed from the constant pool by the argument. The accessed member is then pushed onto the op-stack. An error is thrown if the given member does not exist in this object or if it is not accessible from this scope. |                                                                      `[obj] -> [obj.field]` |
| 14     | STORE_EXTERNAL  |               2 | Performs an external member write operation by removing the top two elements from the op-stack and stores the second removed element as a member into the first one by name. The name is accessed from the constant pool by the argument. An error is thrown if the given member does not exist in this object, if it is not accessible from this scope or if the field is immutable. |                                                                     `[obj, to_store] -> []` |
| 15     |  LOAD_INTERNAL  |               1 |                                                                                                                                                                                              Performs an internal member read operation of `this` by index and pushes the accessed element onto the stack. The index is the 1 byte argument. Visibilities are not taken into account. |                                                                        `[] -> [this.field]` |
| 16     | STORE_INTERNAL  |               1 |                                                                                                                                                                    Performs an internal member write operation by removing the top element from the op-stack and stores it as a member of `this` by index. The index is the 1 byte argument. Visibilities are not taken into account. |                                                                          `[to_store] -> []` |
| 17     |   LOAD_STATIC   |               2 |                                                                                                                Loads a static field by name and pushes it onto the op-stack. The accessed object is `this`. The name is accessed from the constant pool by the argument. An error is thrown if the given member does not exist in this type. Visibilities are not taken into account. |                                                               `[] -> [static_field_of_obj]` |
| 18     |  STORE_STATIC   |               2 |                                                              Removes the top element from the op-stack and stores it into the static field of `this` by name. The name is accessed from the constant pool by the argument. An error is thrown if the given member does not exist in this type. An error is thrown if the field is immutable. Visibilities are not taken into account. |                                                               `[static_field_of_obj] -> []` |
| 19     | CONTAINER_READ  |               0 |                                                                                                                  Removes the top two elements from the op-stack and accesses the first removes as a container with the second one as the key. The accessed value is then pushed onto the op-stack. An error is thrown if the accessed object is not accessible via the `[]` operator. |                                                               `[container, key] -> [value]` |
| 20     | CONTAINER_WRITE |               0 |                                                                                                                                            Removes the top three elements from the op-stack and stores the third one (the value) into the first one (the container) with the second one as the key. An error is thrown if the accessed object is not writeable via the `[]` operator. |                                                             `[container, key, value] -> []` |
| 21     |  LOAD_ABSTRACT  |               2 |                                                                                                                                                                                                 Loads the implementation of an as abstract defined function by name. The name is accessed from the constant pool by the argument. Throws an error if the implementation is not found. |                                                            `[] -> [abstract_function_impl]` |
| 22     |       ADD       |               0 |                                                                                                                                                                             Removes the top two elements from the op-stack and performs an addition on them. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 23     |       SUB       |               0 |                                                                                                                                                                           Removes the top two elements from the op-stack and performs a subtraction on them. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 24     |       MUL       |               0 |                                                                                                                                                                        Removes the top two elements from the op-stack and performs a multiplication on them. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 25     |       DIV       |               0 |                                                                                                                                                                              Removes the top two elements from the op-stack and performs a division on them. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 26     |      IDIV       |               0 |                                                                                                                                                                     Removes the top two elements from the op-stack and performs an integer division on them. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 27     |       MOD       |               0 |                                                                                                                                                                      Removes the top two elements from the op-stack and performs a modulo operation on them. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 28     |       POW       |               0 |                                                                                                                                                                                              Removes the top two elements from the op-stack and powers them. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                              `[exponent, base] -> [result]` |
| 29     |       AND       |               0 |                                                                                                                                                                        Removes the top two elements from the op-stack and performs an and operation on them. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 30     |       OR        |               0 |                                                                                                                                                                         Removes the top two elements from the op-stack and performs an or operation on them. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 31     |       XOR       |               0 |                                                                                                                                                                         Removes the top two elements from the op-stack and performs a xor operation on them. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 32     |       NOT       |               0 |                                                                                                                                                                             Removes the top element from the op-stack and performs a logical negation on it. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                      `[bool] -> [not bool]` |
| 33     |       LT        |               0 |                                                                                                                                             Removes the top two elements from the op-stack and compares them if the left element is less than the right one. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 34     |       GT        |               0 |                                                                                                                                          Removes the top two elements from the op-stack and compares them if the left element is greater than the right one. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 35     |       LEQ       |               0 |                                                                                                                                    Removes the top two elements from the op-stack and compares them if the left element is less or equal than the right one. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 36     |       GEQ       |               0 |                                                                                                                                 Removes the top two elements from the op-stack and compares them if the left element is greater or equal than the right one. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 37     |       SLA       |               0 |                                                                                                                                                     Removes the top two elements from the op-stack and performs a left arithmetical shift operation on them. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 38     |       SRA       |               0 |                                                                                                                                                    Removes the top two elements from the op-stack and performs a right arithmetical shift operation on them. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 39     |       SRL       |               0 |                                                                                                                                                         Removes the top two elements from the op-stack and performs a right logical shift operation on them. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                 `[right, left] -> [result]` |
| 40     |     EQUALS      |               0 |                                                                                                                                                                                                                                                     Removes the top two elements from the op-stack and compares them for their equality. The result is then pushed onto the op-stack. |                                                                 `[right, left] -> [result]` |
| 41     |   NOT_EQUALS    |               0 |                                                                                                                                                                                                                                                   Removes the top two elements from the op-stack and compares them for their inequality. The result is then pushed onto the op-stack. |                                                                 `[right, left] -> [result]` |
| 42     |       NEG       |               0 |                                                                                                                                                                                     Removes the top element from the op-stack and performs a negation on it. The result is then pushed onto the op-stack. An error is thrown if the operation is not applicable on the two arguments. |                                                                     `[number] -> [+number]` |
| 43     |       POS       |               0 |                                                                                                                                                   Removes the top element from the op-stack and signs it with `+`. The result is then pushed onto the op-stack. Effectively this operation has no effect. An error is thrown if the operation is not applicable on the two arguments. |                                                                     `[number] -> [-number]` |
| 44     |    GET_TYPE     |               0 |                                                                                                                                                                                                                                                                                                      Removes the top element from the op-stack and pushes its type onto the op-stack. |                                                                    `[obj] -> [type_of_obj]` |
| 45     |   MAKE_ARRAY    |               1 |                                                                                                                                                                                                                                                         Removes n top elements of the op-stack and stores them into a new array. The first element is the first element in the array. |                                                                  `[1, 2, 2] -> [[1, 2, 3]]` |
| 46     |    MAKE_DICT    |               1 |                                                                                                                                         Removes the 2 * n elements from the op-stack. for each 'tuple' the first element represents the key and the second one the value. Each pair is then stored into a new dictionary. The first removed tuple is the first one in the dictionary. |                                              `["hi", 1, "bye", 2] -> [{"hi": 1, "bye", 2}]` |
| 47     |   MAKE_RANGE    |               0 |                                                                                                                                                   Removes the top two elements from the op-stack and creates a new range from them. The first removed element is the 'from' value and the second value the 'to' value. Throws an error if one of the two arguments is not an integer. |                                                                   `[from, to] -> [from:to]` |
| 48     |      GOTO       |               2 |                                                                                                                                                                                                                                                                                                                   jumps to the absolute address given by the 2 byte integer argument. |                                                                                  `[] -> []` |
| 49     | BRANCH_IF_TRUE  |               2 |                                                                                                                                                                                                                            Removes the top element from the op-stack and checks for its truth value. If it is true an absolute jump to the given 2 byte integer address is performed. |                                                                                  `[] -> []` |
| 50     | BRANCH_IF_FALSE |               2 |                                                                                                                                                                                                                           Removes the top element from the op-stack and checks for its truth value. If it is false an absolute jump to the given 2 byte integer address is performed. |                                                                                  `[] -> []` |
| 51     |     GET_ITR     |               0 |                                                                                                                                                                                                                              Removes the top element from the op-stack and pushes it's iterator object onto the stack. Throws an error if the given object is not an iterable object. |                                                                     `[obj] -> [itr_of_obj]` |
| 52     |   BRANCH_ITR    |               2 |                                                                                                                                                                    Peeks the top element of the op-stack as an iterator object and checks if it has a next element. If not the iterator is removes from the op-stack and an absolute jump to the 2 byte integer address is performed. |                                        if true: `[itr] -> [itr]`<br>if false: `[itr] -> []` |
| 53     |    ITR_NEXT     |               0 |                                                                                                                                                                                                                                                                            Peeks the top element from the op-stack as an iterator object and pushes its next value onto the op-stack. |                                                          `[itr] -> [next_itr_element, itr]` |
| 54     |    ENTER_TRY    |               2 |                                                                                                                                                                                                                                                                                     Maks that an try block has been entered by storing the escape address in case of an caught error. |                                                                                  `[] -> []` |
| 55     |    LEAVE_TRY    |               0 |                                                                                                                                                                                                                                                                                                                                                        Unmarks the entered try block. |                                                                                  `[] -> []` |
| 56     |      THROW      |               0 |                                                                                                                                                                                                   Removes the top element from the op-stack and throws this element as an exception. If the backtracking ends in a try block the thrown value is pushed back onto the (new) op-stack. |                                                                           `[throwed] -> []` |
| 57     |   CALL_VECTOR   |               1 |                                                                                                    Removes the top element from the op-stack as the function and n further elements as the arguments The amount of parameters is specified by the 1 byte argument. Throws an error if the called object is not callable or if the argument amount does not match the parameter count. |                                                                  `[func, p1, p2, p3] -> []` |
| 57     |   CALL_MAPPED   |               1 |                                                           Removes the top element from the op-stack as the called function and n more mapped-arguments, crated by `TO_MAP_ARG` and `TO_NO_MAP_ARG`. The amount of parameters is specified by the 1 byte argument. Errors are the same as for `CALL` instruction, including naming checks, meaning if a required parameter is not set. |                                                                  `[func, param_dict] -> []` |
| 58     |   TO_MAP_ARG    |               2 |                                                                                                                                                                                                                    Removes the top element from the op-stack and pushes it back as a mapped-argument. The mapping-name is loaded from the constant pool by the 2 byte index argument. |                                                            `[obj] -> [Argument{name->obj}]` |
| 58     |   TO_INP_ARG    |               0 |                                                                                                                                                                                                                                                                                                  Removes the top element from the op-stack and pushes it back as an inplace-argument. |                                                                  `[obj] -> [Argument{obj}]` |
| 59     |     RETURN      |               0 |                                                                                                                                                                                                                           Removes the top element from the op-stack and returns it from the function. The returned element is then pushed onto the op-stack of the previous function. | in from returned function: `[return_value] -> []`<br>in to returned: `[] -> [return_value]` |
| 60     |   CALL_SUPER    |               1 |                                                                                                                                                                                                  Removes the top n elements from the op-stack and passes them to the function call of the super constructor of `this`. The amount of parameters is determined by the 1 byte argument. |                                                                  `[func, p1, p2, p3] -> []` |
| 61     |     IMPORT      |               2 |                                                                             Loads a new or exising module into the module area for later access and pushes it onto the op-stack. If the module is not executed yet it will now. The module path is loaded from the constant pool by the given 2 byte index. Throws an error if the module does not exist or a loading error occurred. |                                                                     `[] -> [loaded_module]` |
| 62     |       USE       |               0 |                                                                                                                                                                                 Removes the top element from the op-stack as a module or namespace and stores all its members into the local name registers. Throws an error if the given object is neither a module nor a namespace. |                                                                                  `[] -> []` |
| 63     |    LOAD_NAME    |               2 |                                                                                           Loads an element from the name registers, by searching from the current local scope upwards to the most outer accessible scope, followed by the global scope. The name is loaded from the constant pool by the 2 byte index argument. Throws an error if the looked up name does not exist. |                                                                                  `[] -> []` |
| 64     |       DUP       |               0 |                                                                                                                                                                                                                                                                                                                Duplicates the top element and pushes the duplicate onto the op-stack. |                                                           `[element] -> [element, element]` |


## The Tscript Virtual Machine
The *Tscript Virtual Machine* reads in the required bytecode files at start up and at runtime and
executes them.

### Agenda
- How to use
- Boot procedure
- Dynamic Linking and the Module System
- Memory Layout
  - Modules
  - Runtime Constant Pool
  - Thread Area
  - Call Stack
  - Frames
  - Operand Stack
  - Escape Stack
  - Heap Area
  - Object Structure
    - Member Map
    - Base Object
    - Type Object
    - Callable Object
    - Variable Object
    - Iterable Objects
    - Iterator Objects
    - Other Built-In Objects
- Control Flow
  - Execution Cycle
  - Error Handling
  - Calling Procedure
    - Virtual Function Calls
    - Type Calls
    - Native Function Calls
- Memory Management System
  - Reference Counting
  - Tracing Garbage Collection
- Threading
  - System Threads (light weight)
  - Virtual Threads (user level)

### How to use
To run a compiled tscript file simply call
```bash
tscript compiled.tscriptc arg1 arg2
```
where `arg1` and `arg2` are optional arguments, passed as
command line arguments.

### Boot procedure
Consider the following `example.tscript` file:
```javascript
print("Hello World");
```

After compiling the following steps are performed
- Initializing
- File Loading
- Execution

#### Initializing
The first step in the VM startup is the initialization where
all requirements for the VM are loaded.<br>

#### File Loading
In this Phase the given file is loaded as a new Module.
The loading process includes reading the bytecode and its
validation and setting the ROOT_PATH. (see also Dynamic Linking and the Module System)

#### Execution
In this Phase the main function of the loaded Module is executed.
The main function is determined by the entry point, encoded in the
bytecode. This will then call the `print` function.

### Dynamic Linking and the Module System
The *Tscript Virtual Machine* supports dynamic linking for
loading Modules at runtime.<br>
A module holds the content of a Bytecode File.<br>
Whenever an `import` statement comes up the VM first looks 
weather the given module is already imported. If not the module 
is loaded. In this loading process the VM looks up the corresponding
bytecode file. It searches in the ROOT_PATH, which by default
is the path in which the first loaded module lays. A module is never 
re-read twice.

### Memory Layout
The *Tscript Virtual Machine* is divided in multiple parts of memory.

#### Modules
Modules are important for simplifying the access of external Bytecode Files and File dependencies.
A Module is an object that contains all necessary information for the execution of a specific
Bytecode File.<br>
This includes:
- The <b>Constant</b> Pool for accessing file specific constants
- <b>Functions</b> that are defined in this module
- <b>Types</b> that are defined in this Module
- <b>Global Name Registers</b> representing global variables accessible via index and name. 
The amount of global variables is fix.
- <b>Used Name Registers</b> for holding global variables that were added with the `use` statement. 
Therefore, the amount of Name Registers is variable.

### Runtime Constant Pool
The Runtime Constant Pool (RCP) is loaded from the Constant Pool that is
encoded in the Bytecode.
The RCP holds Constants that are used for the file in which it is encoded.
These Constants can be accessed by index. The loading of a constant
depends on the constant type.

### Thread Area
The Thread Area is responsible for creating and starting as well as
interrupting, stopping, pausing and accessing running threads.

### Call Stack
The call stack is part of a thread and keeps track of function calls.
The state of A function stored as an element in the call stack is called
a 'Frame'.<br>
If the call stacks size exceeds a defined threshold a StackOverflowError
is thrown.

### Frames
A Frame holds the state of a function execution.
This includes:
- The operand stack
- The stack pointer (sp), pointing to the top element of the operand stack.
- A reference to the instruction bytecode of the current function
- The instruction Pointer (ip), pointing to the next instruction to execute.
- A reference to the module in which the function lays
- A reference to the owner of this method (could also be 'this' for global functions or lambdas)
- The escape stack
- The escape pointer (ep)
- The current line number being executed

### Operand Stack
The Operand Stack is responsible for caching the current processed data. The
top of the element is the most relevant for the next instruction.
The top element is determined by the stack pointer (sp) pointing to the top element
of the stack. If an element is pushed, first the sp gets increased by one. Then
the element is assigned to that location to which the sp points to. If the top element is
removed the sp gets decreased by one.

### Escape Stack
The Escape Stack Keeps track of potential jump addresses to which can be jumped if an
error is thrown inside a try-block. If a jump was necessary to escape the error and the 
escape stack is not empty, the instruction pointer is set to the top jump address.
The top jump address then is removed.<br>
To determine the top element of the escape stack the escape pointer (ep) points to the top
element. If the stack is empty the ep is -1.

### Heap Area
The heap area is the main memory when it comes to object management. The heap is responsible
for allocating and freeing memory. It closely works together with the garbage collector.

### Object Structure
An Object represents a chunk of data, that is processed in the execution process. In Tscript
everything is an object.

#### Member Map
Before we talk about the different object types and their structure we first have to look at the Member Map.
The Name Map maps names, encodes as string, to a pointer at which the associated member. Each member
contains:
- Its index
- Its visibility
- Its Mutability

The Member Map is structured as follows:
```c
#define PUBLIC 0
#define PROTECTED 1
#define PRIVATE 2

typedef struct Member {
    BaseObject* content;
    int index;
    int visibility;
    int mutable;
} Member;

typedef struct MapNode {
    int hasMember;
    Member* member;
    char c;
    struct MapNode* children;
    int childCount;
} MapNode;

typedef MapNode MemberMap;
```

Note how the MemberMap is structured as a tree where each Node consist of a character.
This is because it reduces access time from O(n^2) to O(n).
<p>
Member Maps are used by every Object to store their members/fields;

#### Base Object
The Base Object is the root object for all other object types.
It has the following structure:
```c
typedef struct BaseObject {
    TypeObject* type;
    int refCount;
    MemberMap* names;
} BaseObject;
```

#### Type Object
A Type Object stores the actual object type. Every object stores its object type.
Type Objects can not get garbage collected.
```c
typedef struct TypeObject {
    CallableObject super;
    char* name;
    TypeObject* superType;
    int isAbstractType;
    int isCallableType;
    int isContainerAccessible;
    int isContainerWriteable;
    CallableObject* constructor;
    MemberMap* memberDefs;
} TypeObject;
```

#### Callable Object
A Callable Type defines a superior type for all others callable objects.
```c
typedef struct CallableObject {
    BaseObject super;
    int isVirtual;
} CallableObject;
```

There exist three types of callable objects:
- Type Objects (see above)
- Virtual Function Objects
- Native Function Objects
<p>
The Virtual Function Object represents a function instance
that is defined in the bytecode.

```c
typedef struct VFunctionObject {
    CallableObject super;
    BaseObject* owner;
    ModuleObject* module;
    int stackSize;
    int localSize;
    char** instructions;
    MemberMap* defaults;
} VFunctionObject;
```

Note that `defaults` only uses `content` for the default value and `index`
for the parameter location.

#### Variable Objects
Objects that change in size are considered to extend `VariableObject`
```c
typedef struct VariableObject {
    IterableObject super;
    int size;
} VariableObject;
```

There exist two types of variable objects:
- Array Objects
- Dictionary Objects

Array Objects are structured as follows:
```c
typedef struct ArrayObject {
    VariableObject super;
    BaseObject** content;
    int length;
} ArrayObject;
```

where Dictionary Objects are structured like this:
```c
typedef struct DictObject {
    VariableObject super;
    
} DictObject;
```

#### Iterable Objects
Iterable Objects are objects that are able to provide a iterator object for iteration.
This get important in for-loops in which the iterator is used.

```c
typedef struct IterableObject {
    BaseObject super;
    IteratorObject* (*getNewIterator)();
} IterableObject;
```

#### Iterator Objects 
Iterator Objects are objects that are produced by Iterable Objects.
They hold the current state for a iteration.
```c
typedef struct IteratorObject {
    BaseObject super;
    int (*hasNext)();
    BaseObject* (*next)();
} IteratorObject;
```

#### Other Built-In Objects
Integer:
```c
typedef struct IntObject {
    BaseObject super;
    int value;
} IntObject;
```

Real:
```c
typedef struct RealObject {
    BaseObject super;
    double value;
} RealObject;
```

Boolean
```c
typedef struct BoolObject {
    BaseObject super;
    int value;
} BoolObject;
```

String:
```c
typedef struct StrObject {
    IterableObject super;
    char* value;
} StrObject;
```

Range:
```c
typedef struct RangeObject {
    IterableObject super;
    int from;
    int to;
} RangeObject;
```

### Control Flow

#### Execution Cycle
Instructions are introduces via the fetch-decode-execute cycle.<br>
First the next Instruction is fetched from the top Frame of the call stack.
Then the opcode is read to determine which action has to be performed, after which
the corresponding action is executed with the given arguments of the instruction.
A Thread executes only one instruction at a time.<br>
The FDE cycle could look something like this:
```c
while (1) {
    char* instruction = fetchNext();
    char opcode = instruction[0];
    switch(opcode) {
        case PUSH_NULL:
            push_op_stack(NULL);
            break;
        ...
    };
}
```

#### Error Handling
When an error occurs it first checks weather it is thrown inside a try-block.
To determine if a function is inside a try-block the escape stack of the top frame
is checked for non-emptiness. If it is not empty the error is escaped and the ip i set to
the top of the top address of the escape stack. Otherwise, the top frame is removed and the same
procedure is repeated until either I) the call stack is empty or II) a try-block is detected.
If the call stack is empty. The thread prints the error and the stack trace and terminates.
The stack trace is determined while going back from one function to the previous one.
It shows the order of called functions from the main function to the function in which the error
occurred.<br>
Pseudocode:
```c
void throw(BaseObject* thrown){
    Frame* frame = get_top_frame();
    
    while(frame != NULL && frame->ep == -1){
        // backtrack until either the call stack is empty
        // or a try-block is reached
        pop_top_frame();
        frame = get_top_frame();
    }
    
    if (frame != NULL) {
        // try-block reacked
        jump_to_(frame->escape_stack[frame->ep--]);
        push_op_stack(thrown);
    }
    else {
        // call stack is empty
        print(thrown);
        terminate_thread();
    }
}
```

#### Calling Procedure
When a call instruction is executed it first analyses the called object.
If it is not callable an error is thrown.
Otherwise, it is checked weather the called object is a Type. If so a type call
is performed. Otherwise, it is checked weather the called object is a
virtual or a native function. Depending on the type either a virtual or a native call
is performed.

##### Virtual Function Calls
If a virtual call is performed first the provided parameters are loaded from the operand stack.
Next it is checked weather all required parameters are loaded. This includes filling the
missing parameters with the default values. If the argument count does not equal the 
parameter count an error is thrown. If not a new Frame is created and pushed onto the 
call stack. All provided arguments are then pushed onto the operand stack of the new frame,
where the first parameter is on top.
If the stack reaches a defined threshold a StackOverflowError is thrown.

##### Type Calls
If a type call is performed first a new object of that type is created.
If a constructor is given for that type it then is evaluated on the object.
The constructor is either a virtual or a native method.

##### Native Function Calls

### Memory Management System
The *Tscript Virtual Machine* performs automated memory management.
It supports two algorithms to do so.

#### Reference Counting
Every time an object is assigned to a register its reference count is 
increased and decreased if it is displaced.
If its reference count drops down to 0 the object is not reachable anymore
and can get freed.

#### Tracing Garbage Collection
With reference counting circular references can not get detected, 
lading to memory leaks. To avoid this the tracing garbage collector
is used. It collects all references in all currently stored registers
in the VM. From there all reachable references can be determined and
therefore also all unreachable references, as in circular references,
can be detected and dissolved. Since the collection of all roots
might clash with parallel thread execution all threads are paused until
the garbage collector finishes.

### Threading
The VMs execution is based on Threads. Each Threads represents
an independent interpreter with its own state. All Threads share global resources, as well
as the heap memory. There two thread types:

#### System Threads (light weight)
A System thread is a thread that is requested by the underlying operating system.
System threads enables a faster execution due to real parallelism.
This thread type is the default thread type when a new thread is created and
no explicit virtualization is specified. If the system runs out of threads
a Virtual Thread is created instead.

#### Virtual Threads (user level)
Virtual Threads are threads that are managed and scheduled by the VM itself.
Virtual treads tend to be slower since they do not run truly in parallel,
but they allow to create as many threads as necessary for the application.