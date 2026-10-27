
## What's new?

This section shows all new features this tscript implementation provides.

### Dictionaries
Dictionaries now allow other key types than `String`, for example:
```javascript
var x = {1: true, null: "text"};
print(x[null]); # prints: text
```


### typeof Keyword
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

### Switch Case Statement
Tscript now supports switch-case statements:

```javascript
switch "Tommy" then {
    case "Tommy" do print("I love Cake!");
    case "Jane" do {
        print("I love Chocklate!");
        print("Really!");
        break; # skip all remaining cases + default
    }

    # Evaluated at the end.
    # The evaluation of the cases has no effect on the default evaluation.
    default print("no one selected :(");
}
```

### Abstract Types

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
    public: abstract function bar;
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
    public: native function bar(); # overrides abstract function bar in A
}
```

### Enums
Tscript now supports enums. For example:
```javascript
enum LogType {
    NORMAL,
    WARNING,
    INFO,
    ERROR
}

print(LogType.NORMAL); # prints: NORMAL
```

### _\_str\__ Method

Tscript now supports `__str__` methods. If a class implements this
method, an object of that class is printed as the return value of this
`__str__` method.

### Native Functions

With the new Tscript implementation the `native` keyword is
introduced. It allows to define functions that are implemented
and executed from the underlying execution engine. <p>

Assuming the underlying Virtual Machine is implemented in C,
a native function, and it's implementation, might look like this::<br>

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

### const Keyword
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


### Imports
Tscript now supports the `import` keyword, allowing the usage of other files.<p>
utils.tscript
```javascript
native function print(x="");
```
main.tscript
```javascript
import utils;
import utils.print; # This is also possible!

utils.print("hi!");
print("hi again!");
```

You also may use the keyword `from` to specify your import.
```javascript
from utils import print;

print("hi");
```

It's important to note that an import of a module, which is
not imported jet, triggers the main function of that imported module
for initialization, similar to Python.

In order to start a script or load a module properly it is required that each
tscript file begins with `module <module-name> ;`.
This module name can differ from the file name.<p>

Imports are resolved dynamically at runtime.

### Custom Main Function

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

### Based Integers

Integers can now be written in binary, octal and hexadecimal format:
```javascript
var decimal = 8;
var binary = 0b1000; # equal to 8
var octal = 0o10; # equal to 8
var hex = 0xF; # equal to 15
```

### Shift Operators

With the new Integer representation also comes the new shift operators:
```javascript
print(1 << 1); # prints: 2
print(4 >> 1); # prints: 2
```
