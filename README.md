# MiniLang

A modern, fast, memory-safe, and beginner-friendly programming language workbench built entirely in Java 21+.

MiniLang combines the conceptual simplicity of Python and Scratch with the architectural rigor of a production stack-based virtual machine, featuring a clean browser-based Web Studio, live syntax search, and zero external runtime dependencies.

> 📖 **Deep Technical Architecture**: For a full technical breakdown of the lexer, AST, bytecode opcodes, constant folding, and virtual machine unwinding, see [explanation.md](file:///home/adarsh/Documents/College%20Things/Minilang/explanation.md).

---

## Architecture Pipeline

```text
Source Code (.ml) or Interactive REPL / Web Studio
  ↓
Lexer (Tokens, operators, string scanners, comments)
  ↓
Parser (Recursive descent → AST, forgiving syntax, desugared loops)
  ↓
Compiler (Two-pass compiler, constant folding optimization, jump backpatching)
  ↓
Stack Virtual Machine (Activation call frames, operand stack, recursion guard, frame cleanup)
  ↓
Execution & Outputs (Console stdout, PNG Chart Plotter, Vector Turtle Graphics, 2D OpenGL)
```

---

## Web Studio & Interactive Playground

MiniLang includes a lightweight, embedded development workbench served directly from the executable JAR.

### Key Features
- **Live Code Editor**: Line numbers, tab indentation, status bar, and real-time syntax highlighting for key language tokens.
- **Searchable Syntax Catalog**: 20 comprehensive reference guides ranging from fundamental Hello World to advanced OOP, File I/O, and exception handling.
- **1-Click Compiler Insertion**: Click any snippet in the documentation to insert it directly into the editor and execute it immediately with <kbd>Ctrl</kbd> + <kbd>Enter</kbd>.
- **Calming Dark Theme**: Dedicated sun/moon toggle switching between a soft warm light palette and a distraction-free, matte dark mode (no gradients, no neon glow).
- **Virtual Machine Inspectors**: Real-time bytecode disassembler tab and Abstract Syntax Tree (AST) hierarchy viewer.

### Launching the Web Studio
```bash
java -jar target/minilang-1.0-SNAPSHOT.jar --web 8080
```
Open **[http://localhost:8080](http://localhost:8080)** in your browser.

### Sharing to Other Devices
1. **On the Same Wi-Fi (Direct & Instant)**:
   Find your computer's local IP address (e.g. `10.21.57.150`), then open:
   ```
   http://10.21.57.150:8080
   ```
   on any phone, tablet, or laptop connected to the same Wi-Fi.
2. **Over the Internet (Public Tunnel)**:
   ```bash
   npx localtunnel --port 8080
   ```
   Open the generated URL on any device worldwide (enter your public IP when prompted for the endpoint password).

---

## Language Syntax Guide: Basics to Advanced

### 1. Fundamentals & Output
```minilang
// Single-line comment
/* Multi-line comment */

// Console Output (say and show are friendly aliases for print)
say "Hello, World!";
show 42;
print("MiniLang is running");

// Variables & Assignment (let is optional)
let score = 100;
score = score + 25;
name = "Alex";

// Type Inspection
print(type(42));       // "integer"
print(type(3.14));     // "float"
print(type("text"));   // "string"
print(type([1, 2]));   // "array"
print(type({}));       // "map"
```

### 2. Operators & String Multiplication
```minilang
// Arithmetic
let sum = 20 + 5;
let quot = 100 / 4;
let rem = 17 % 5;

// String Concatenation & Multiplication
let greeting = "Hello " + "World";
let cheer = ("Hip " * 2) + "Hurray!"; // "Hip Hip Hurray!"
```

### 3. Control Flow & English Logic
```minilang
// Conditionals (parentheses optional)
let score = 85;
if score >= 80 and not (score is 100) {
    say "Great score!";
} else {
    say "Keep going!";
}

// Kid-Friendly Repeat Loops
let step = 1;
repeat 3 times {
    say "Iteration #" + str(step);
    step = step + 1;
}

// While Loops (supports break and continue)
let i = 0;
while i < 10 {
    i = i + 1;
    if i == 3 continue;
    if i == 7 break;
    print(i);
}

// For-In Loops (iterates arrays, ranges, and strings)
for item in ["Apple", "Berry", "Cherry"] {
    say item;
}

for n in range(3) {
    print(n);
}
```

### 4. Functions & Recursion
```minilang
// Function Declaration
fn add(a, b) {
    return a + b;
}
let total = add(15, 25);

// Recursion (protected by VM call-stack guard: MAX_CALL_STACK_DEPTH = 1000)
fn factorial(n) {
    if n <= 1 return 1;
    return n * factorial(n - 1);
}
print(factorial(5)); // 120
```

### 5. Collections: Arrays & Dictionaries
```minilang
// Dynamic Arrays (Lists)
let numbers = [10, 20, 30];
push(numbers, 40);
let last = pop(numbers);
print("Length: " + str(len(numbers)));
print("First:  " + str(numbers[0]));

// Native Dictionaries (HashMaps)
let user = { "name": "Ada", "score": 95 };
user["role"] = "Admin";
print(user["name"]);
print("Has role? " + str(has(user, "role")));
print("Keys:     " + str(keys(user)));
print("Values:   " + str(values(user)));
```

### 6. Object-Oriented Programming (OOP)
```minilang
class Vector2D {
    init(x, y) {
        this.x = x;
        this.y = y;
    }

    magnitude() {
        return sqrt(this.x * this.x + this.y * this.y);
    }

    add(other) {
        return Vector2D(this.x + other.x, this.y + other.y);
    }
}

let v1 = Vector2D(3.0, 4.0);
print("Magnitude: " + str(v1.magnitude())); // 5.0

let v2 = Vector2D(1.0, 2.0);
let v3 = v1.add(v2);
print("v3.x: " + str(v3.x)); // 4.0
```
*Note: Both `this` and `self` are valid receiver keywords.*

### 7. Exception Handling (`try ... catch`)
```minilang
try {
    let result = 100 / 0;
} catch (err) {
    print("Caught runtime error: " + err);
}

// Function with internal recovery
fn safeDivide(a, b) {
    try {
        return a / b;
    } catch (e) {
        return 0;
    }
}
print(safeDivide(50, 0)); // 0
```

### 8. File I/O Built-ins
```minilang
// Write or overwrite file
writeFile("audit.log", "Initial log line\n");

// Append to file
appendFile("audit.log", "Second log line\n");

// Read entire file content
let content = readFile("audit.log");
print(content);

// File existence & deletion
print("Exists? " + str(fileExists("audit.log")));
deleteFile("audit.log");
```

### 9. Multi-line Raw Strings
```minilang
let banner = """
+-----------------------------+
| MiniLang Language Workbench |
+-----------------------------+
""";
print(banner);
```

---

## Standard Library Reference

### Core Built-ins
| Function | Signature | Description |
| :--- | :--- | :--- |
| `print` / `say` / `show` | `(value)` | Outputs value to console with newline |
| `input` | `()` | Reads line from standard input |
| `str` | `(val)` | Converts value to string |
| `int` | `(val)` | Converts value to integer |
| `float` | `(val)` | Converts value to float |
| `type` | `(val)` | Returns runtime type name |
| `clock` | `()` | Timestamp in seconds |

### Arrays & Dictionaries
| Function | Signature | Description |
| :--- | :--- | :--- |
| `len` | `(arr \| map \| str)` | Number of elements or character count |
| `push` | `(arr, item)` | Appends item to array |
| `pop` | `(arr)` | Removes and returns last item |
| `has` | `(map, key)` | Returns `true` if key exists |
| `keys` | `(map)` | Returns array of map keys |
| `values` | `(map)` | Returns array of map values |

### File I/O
| Function | Signature | Description |
| :--- | :--- | :--- |
| `readFile` | `(path)` | Reads complete file as UTF-8 string |
| `writeFile` | `(path, data)` | Creates or overwrites file |
| `appendFile` | `(path, data)` | Appends text to file |
| `fileExists` | `(path)` | Returns boolean file presence |
| `deleteFile` | `(path)` | Deletes file from filesystem |

### Mathematical Built-ins
| Function | Signature | Description |
| :--- | :--- | :--- |
| `sqrt` | `(num)` | Square root |
| `pow` | `(base, exp)` | Power ($b^e$) |
| `abs` | `(num)` | Absolute value |
| `min` / `max` | `(a, b)` | Minimum / Maximum of two numbers |
| `round` / `floor` / `ceil` | `(num)` | Rounding utilities |
| `sin` / `cos` | `(rad)` | Trigonometric radians |
| `random` | `()` | Pseudo-random float in $[0.0, 1.0)$ |

### Fast Algorithms (`import algo`)
| Function | Signature | Description |
| :--- | :--- | :--- |
| `range` | `(stop)` or `(start, stop, step)` | Builds numeric sequence array |
| `sort` | `(array, [ascending=true])` | Dual-pivot Quicksort |
| `reverse` | `(array \| str)` | Reverses array or string |
| `sum` | `(array)` | Sum of array numbers |
| `mean` | `(array)` | Mathematical average |
| `median` | `(array)` | Median value |
| `binarySearch` | `(sortedArray, target)` | $O(\log n)$ search returning index or `-1` |

### Scientific Chart Plotter (`import plot`)
- `plotLine(y)` or `plotLine(x, y, [label])`: Line graphs
- `plotScatter(x, y, [label])`: Scatter charts
- `plotBar(categories, values, [label])`: Categorical bar charts
- `plotTitle(text)`, `plotXLabel(text)`, `plotYLabel(text)`: Chart labels
- `plotGrid(bool)`: Toggle background grid
- `plotSave(filePath, [w=700], [h=500])`: Renders and exports chart to PNG
- `plotShow(w, h)`: Displays interactive chart window

### Turtle Vector Graphics (`import turtle`)
- `forward(dist)` (alias `fd`), `backward(dist)` (alias `bk`)
- `turnRight(deg)` (alias `rt`), `turnLeft(deg)` (alias `lt`)
- `penUp()` (`pu`), `penDown()` (`pd`), `penColor(r, g, b)`, `penSize(px)`
- `turtleCircle(radius)`, `turtleClear()`, `turtleSpeed(ms)`
- `turtleSave(filePath)`: Exports vector drawing to PNG

### 2D OpenGL Engine (`minilang.graphics`)
- Double-buffered immediate mode: `glBegin("TRIANGLES")`, `glVertex(x, y)`, `glEnd()`
- Primitives: `glClear`, `glColor`, `glRect`, `glFillRect`, `glCircle`, `glLine`, `glText`
- Event polling: `glKeyDown(key)`, `glMouseX()`, `glMouseY()`, `glMouseDown()`

---

## Quick Start & CLI Tools

### Build Shaded Executable JAR
```bash
mvn clean package
```

### Run the Interactive Web Studio
```bash
java -jar target/minilang-1.0-SNAPSHOT.jar --web 8080
```

### Interactive REPL
```bash
java -jar target/minilang-1.0-SNAPSHOT.jar --repl
```

### Execute Scripts
```bash
# Advanced Showcase (Dictionaries, File I/O, Try/Catch, Multi-line Strings)
java -jar target/minilang-1.0-SNAPSHOT.jar examples/advanced_demo.ml

# Kid-Friendly Syntax Demo
java -jar target/minilang-1.0-SNAPSHOT.jar examples/kid_friendly_demo.ml

# Fast Algorithms & Statistics Demo
java -jar target/minilang-1.0-SNAPSHOT.jar examples/algorithms_demo.ml

# Scientific Chart Plotter Demo
java -jar target/minilang-1.0-SNAPSHOT.jar examples/plot_demo.ml

# Turtle Vector Art Demo
java -jar target/minilang-1.0-SNAPSHOT.jar examples/turtle_demo.ml

# Object-Oriented Programming Demo
java -jar target/minilang-1.0-SNAPSHOT.jar examples/oop_demo.ml
```

### Compiler Inspection Tools
```bash
# View disassembled bytecode instructions
java -jar target/minilang-1.0-SNAPSHOT.jar --bytecode examples/advanced_demo.ml

# Inspect Abstract Syntax Tree (AST)
java -jar target/minilang-1.0-SNAPSHOT.jar --ast examples/advanced_demo.ml

# View Token Stream
java -jar target/minilang-1.0-SNAPSHOT.jar --tokens examples/advanced_demo.ml
```

---

## Automated Test Suite

MiniLang includes **49 automated unit tests** across 8 test suites:

```bash
mvn test
```

- `AdvancedFeaturesTest`: Dictionaries, File I/O, Try/Catch exception unwinding, Multi-line strings.
- `KidFriendlyAndLibrariesTest`: Auto-assignment, English operators, `repeat`, `for-in`, `algo`, `plot`, `turtle`, `import`.
- `Features2Test`: Floating point math, array push/pop, break/continue, string indexing, math library.
- `OopAndSecurityTest`: Classes, `init`, methods, `this`/`self`, dynamic properties, recursion limit guard, frame cleanup.
- `CompilerTest`: Bytecode emission, constant folding optimization, backpatching.
- `VMTest`: Stack operations, arithmetic, comparisons, dynamic lists, recursion.
- `ParserTest`: Grammar precedence, statement parsing, block structures.
- `LexerTest`: Tokenization, comments, string literals, keywords.

---

## Project Structure

```
minilang/
├── explanation.md             (Complete architectural and technical decoding)
├── README.md                  (User guide, syntax & standard library reference)
├── pom.xml                    (Maven build configuration with Java 21)
├── web/                       (Web Studio static assets: HTML, CSS, JS)
├── examples/                  (Curated runnable example scripts)
│   ├── advanced_demo.ml       (Dictionaries, File I/O, Try/Catch, Multi-Strings)
│   ├── kid_friendly_demo.ml   (Kid-friendly variables, repeat loops, English logic)
│   ├── algorithms_demo.ml     (Sorting, statistics, range, binary search)
│   ├── plot_demo.ml           (Line, scatter, and bar charts saved to PNG)
│   ├── turtle_demo.ml         (Geometric star spiral art saved to PNG)
│   ├── import_demo.ml         (Modular import demonstration)
│   ├── math_helper.ml         (Reusable module with functions and a class)
│   ├── oop_demo.ml            (Classes, init, methods, this/self)
│   ├── graphics_demo.ml       (OpenGL-style immediate mode 2D window)
│   └── showcase.ml            (Language feature showcase)
└── src/
    ├── main/
    │   ├── resources/web/     (Packaged web assets inside shaded JAR)
    │   └── java/minilang/
    │       ├── Main.java      (CLI entry point & runner)
    │       ├── algo/          (Fast algorithm library: AlgoLib.java)
    │       ├── compiler/      (Compiler.java, Opcode.java, Bytecode.java)
    │       ├── graphics/      (GraphicsWindow.java 2D OpenGL immediate mode)
    │       ├── lexer/         (Lexer.java, Token.java, TokenType.java)
    │       ├── parser/        (Parser.java, AST records: MapLiteral, TryCatch, etc.)
    │       ├── plot/          (PlotEngine.java scientific charting engine)
    │       ├── repl/          (Repl.java interactive shell)
    │       ├── turtle/        (TurtleEngine.java interactive turtle graphics)
    │       ├── vm/            (VM.java, MapValue, MLClass, MLInstance, Builtins)
    │       └── web/           (WebServer.java embedded HTTP studio server)
    └── test/java/minilang/    (49 automated unit tests)
```

---

## License

Educational / MIT.
