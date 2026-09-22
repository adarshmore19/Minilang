# MiniLang

A small, educational, self-contained programming language implementation in Java.

## What is MiniLang?

MiniLang is a toy compiler/VM pipeline built to demonstrate every stage from source code to execution:

```text
Source (.ml)
  ↓
Lexer (tokens)
  ↓
Parser (recursive descent → AST)
  ↓
Compiler (AST → custom bytecode)
  ↓
Stack-Based Virtual Machine
  ↓
Output
```

This is **not** a production language. It exists to learn lexical analysis, parsing, AST design, bytecode, and stack-machine execution.

## Why it exists

As a reference implementation inspired by Robert Nystrom's *Crafting Interpreters*, but with a custom bytecode compiler and stack VM instead of tree-walking interpretation.

## Supported syntax

- Types: `Integer`, `Boolean`, `String`
- Variables: `let x = 10;`, `x = 20;`
- Expressions: `+ - * / %`, comparison `== != < <= > >=`, boolean `&& || !`
- Control flow: `if (cond) { ... } else { ... }`, `while (cond) { ... }`
- Functions: `fn add(a, b) { return a + b; }`
- Recursion supported (validated by `factorial.ml`)
- Built-in: `print(value);`
- Comments: `/* block */`

## Architecture

```
minilang/
├── src/main/java/minilang/
│   ├── Main.java
│   ├── lexer/     (Lexer, Token, TokenType)
│   ├── parser/    (Parser, Expr, Stmt)
│   ├── compiler/  (Opcode, Instruction, Bytecode, Compiler)
│   └── vm/        (VM, Value, CallFrame, RuntimeError)
├── examples/      (hello.ml, variables.ml, conditions.ml, loops.ml, functions.ml, factorial.ml, final_acceptance.ml)
├── src/test/java/minilang/ (tests for each layer)
├── pom.xml
└── README.md
```

### Bytecode instruction set

| Instruction | Meaning |
|---|---|
| PUSH_CONST | Push literal onto operand stack |
| LOAD_LOCAL / STORE_LOCAL | Read/write local variable slot |
| ADD / SUB / MUL / DIV / MOD | Arithmetic |
| EQ / NE / LT / LE / GT / GE | Comparison |
| AND / OR / NOT | Boolean |
| JUMP / JUMP_IF_FALSE | Control-flow jumps |
| CALL / RETURN | Function call / return |
| PRINT / POP / HALT | I/O and termination |

### VM design

- Operand stack (LIFO)
- Call stack (CallFrame per invocation)
- Instruction pointer (IP) per frame
- Locals array per frame

Function calls restore caller frame, push return value, and continue execution.

## How to build

Requirements:
- Java 21+ (tested with JDK 25 / Temurin)
- Maven (optional; `javac` works for manual compile)

```bash
cd D:\College Thingy\Projects\minilang
mvn compile
```

Or compile manually:

```bash
javac -cp src/main/java -d target src/main/java/minilang/lexer/*.java src/main/java/minilang/parser/*.java src/main/java/minilang/compiler/*.java src/main/java/minilang/vm/*.java src/main/java/minilang/Main.java
```

## How to run

Run a program:

```bash
java -cp target minilang.Main examples/hello.ml
```

Debug modes:

```bash
# Token stream
java -cp target minilang.Main --tokens examples/hello.ml

# AST (structural)
java -cp target minilang.Main --ast examples/hello.ml

# Bytecode disassembly
java -cp target minilang.Main --bytecode examples/factorial.ml
```

## Example programs

- `hello.ml` — `print("Hello, MiniLang!");`
- `variables.ml` — `let x = 10; let y = 20; print(x + y);`
- `conditions.ml` — `if (x > 10) { print("large"); }`
- `loops.ml` — `while (x < 5) { ... }`
- `functions.ml` — user-defined `fn add(a,b) { ... }`
- `factorial.ml` — recursive factorial (expected: `120`)
- `final_acceptance.ml` — full pipeline test (expected: `MiniLang works!`)

## Testing

```bash
mvn test
```

Tests cover: lexer, parser, compiler, VM arithmetic/variables/comparisons/if/while/functions/return/recursion, and end-to-end source→VM.

## Known limitations

- No floating-point types (only int, bool, string)
- No arrays / objects / structs
- No exceptions / modules / imports
- No garbage collection (simple VM, no heap management)
- Minimal standard library (`print` only)
- No `for`, `switch`, `break`, `continue`
- Bytecode is custom (not JVM bytecode)

## Future improvements

- Add `for` loop
- Add array/list support
- Full `VM.java` execution loop (current: structural / compile-time model)
- More comprehensive error messages with source line mapping
- Add `README` generation from grammar spec

## License

Educational / MIT-style for learning purposes.

