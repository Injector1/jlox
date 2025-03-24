# JLox Interpreter

JLox is a Java-based interpreter for the Lox programming language, inspired by the book [Crafting Interpreters](https://craftinginterpreters.com). This project not only implements the core language as described in the book but also extends it with several additional features such as:

- Static methods on classes;
- Anonymous functions (lambdas) for inline declarations;
- Detection of unused variables; 
- REPL supporting both statements and expressions;
- Enhanced string operations (comparisons and concatenation with numbers);
- Extra syntactic sugar (e.g., C-style comma operator, break expressions) for a modern, expressive language.

## Features

Full Lox Language Implementation
- Implements variable declarations, function definitions, classes, inheritance (including super), control flow (if, while, for, break, return) and expressions;
- **Static Methods:** Methods prefixed with the `class` keyword are bound to the class, not to an instance;
- **Lexical Scoping:** Uses environment chains and closures for correct variable resolution;
- **Anonymous Functions:** Lightweight inline functions for callback and functional programming scenarios;
- **Robust Error Handling:** Provides clear error messages during parsing/resolving and at runtime.

## Getting Started

### Prerequisites
- Java (JDK 11 or higher is recommended)
- Gradle (or your favorite build tool)
### Installation
1. Clone the Repository:
```shell
  git clone https://github.com/Injector1/jlox.git && cd jlox
```
2. Build the Project:
If you’re using Gradle:

```shell
  ./gradlew build
```

## Running JLox
JLox can be run in two modes:

### Script Mode:
#### Run a Lox script from a file:

```shell
  java -jar build/libs/jlox.jar path/to/script.lox
```

#### REPL Mode:

Run JLox without arguments to start an interactive prompt:

```shell
  java -jar build/libs/jlox.jar
```

## Example Usage

### Example 1: Class with a Static Method
```
class Math {
    class square(n) {
        return n * n;
    }
}

print Math.square(3);
```
### Example 2: Using Anonymous Functions as Callbacks
```
fun thrice(fn) {
  for (var i = 1; i <= 3; i = i + 1) {
    fn(i);
  }
}

thrice(fun (a) {
  print a;
});
// Expected output:
// 1
// 2
// 3
```
### Example 3: Unused Variable Check
```
{
    var unused = 42;
    print "Hello, world!";
}
// The resolver will report that 'unused' is never used.
```
### Example 4: Simple inheritance
```
class Car {
    beep() {
        print "Beep";
    }
}

class ConcreteCar < Car {}

ConcreteCar().beep();
// Should print "Beep"
```

## Project Structure

- `org.jlox` – Contains the main interpreter, parser, scanner, and runtime components.
- `org.jlox.exception` – Custom exception classes for handling runtime and parse errors.
- `org.jlox.primitives` – Built-in native functions (e.g., clock).
- `org.jlox.tool` – Utility tools such as the AST generator.

## Acknowledgements

[Crafting Interpreters by Robert Nystrom](https://craftinginterpreters.com) – The inspiration and guide for this interpreter.
