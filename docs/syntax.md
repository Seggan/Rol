# Syntax

As can be seen, it is heavily borrowed from Kotlin.

## Done
These are the ones that you can actually run with Rol's current state of development.

### Hello, World!
```
print("Hello, World!")
```

### Math
```
var x = 1
var y = 2.1
print(x / y)
```

### I/O
```
print("What is your name?")
var name = readLine()
print("Hello, " + name)
```

### Branching
```
if (readNumber() % 2 == 0) {
    print("The number is even")
} else {
    print("The number is odd")
}
```

### Function definitions
```
fun double(x: Number): Number {
    return x * x
}

print(double(2))
```

### Nullable types
```
var nullableNum: Number? = 1
print(nullableNum) // 1
nullableNum = null
print(nullableNum) // null
```

### Embedded Lua
This is actual code you can find in the `rio` file of the stdlib.
```
fun print(x: dyn?) {
    extern (x) {
        if x == nil then
            print("null")
        else
            print(x)
        end
    }
}
```

### Lambdas
Function definitions are actually sugared lambdas
```
// Full form
val toString: (Object?) -> String = { (String) x: Object ->
    return magic(x)
}

// No explicit type
val toString = { (String) x: Object? ->
    return magic(x)
}

// Implicit return type
val toString = { x: Object? ->
    return magic(x)
}

print(toString(1))
```

## Planned
This syntax is planned for the time being. It might change.

### Immutable variables
```
val x = 1
x = 2 // compile error
```

### Classes
```
class Complex {
    val real: Number
    val imag: Number
    
    init(real: Number, imag: Number) {
        this.real = real
        this.imag = imag
    }
}
print(Complex(1, 0))
```

### Primary constructors
```
class Complex(val real: Number, val imag: Number) {}
```

### Union/Intersection types
```
type Number = Int | Float | Complex
type Human = Living & Smart
type Car = Automobile // can also be used as type aliases
```

### Loops
```
// C style
for (i = 0; i < 10; i++) {
    print(i)
}

// Foreach
for (i in 0..9) {
    print(i)
}

// While
var i = 0
while (i < 10) {
    print(i)
    i++
}

// Do-while
var i = 0
do {
    print(i)
    i++
} while (i < 10)
```

### Generics
```
fun parseNum(s: String): Result<Number, ParseError> {
    ...
    return Result(parsed)
}
```

### Errors
```
do {
    throw WhateverError()
} catch (e: WhateverError) {
    print("Caught")
}
```

### Effects
Also showcases inheritance
```
class MissingInteger : Effect<Number>() {}

do {
    var x = raise MissingInteger()
    print(x) // 10
} catch (e: MissingInteger) {
    continue using 10
}
```

### Tuples
```
fun pair<X, Y>(x: X, y: Y): (X, Y) {
    return (x, y)
}
```

### A bunch of class related stuff I don't have the time to write
Yknow, the standard: interfaces, overriding, abstract classes, etc. Look at Kotlin.

### Lambdas
These parts still haven't been added
```
// Implicit arg type (requires a type on the variable though)
val toString: (Object?) -> String = { x ->
    return magic(x)
}

// Implicit parameter
val toString: (Object?) -> String = {
    return magic(it)
}

// Implicit return
val toString: (Object?) -> String = { magic(it) }

// Forget the lambda, use magic directly
val toString: (Object?) -> String = magic
```

## Problems
Here are some example problems.

### Factorial
```
fun factorial(x: Number): Number {
    if (x == 1) {
        return 1
    } else {
        return x * factorial(x - 1)
    }
```

### Fibonacci
```
val max = readNumber()
var x = 1
var y = 1
do {
    val temp = x
    x = y
    y = temp + x
    print(x)
} while (y < max)
```

### Selection sort
```
fun sort<E>(list: List<E>): List<E> {
    val new = mutableList<E>()
    val copy = list.toMutableList()
    while (copy.isNotEmpty()) {
        val max = copy.max()
        new.add(max)
        copy.remove(max)
    }
    return new
}
```