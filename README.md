# MiniLang 2.0

A self-contained, educational, fast, secure, and kid-friendly programming language implementation in modern Java 21+.

> 📖 **Deep Technical Architecture**: For an in-depth breakdown of the lexer, parser, compiler, bytecode opcodes, and stack virtual machine, see [explanation.md](file:///home/adarsh/Documents/College%20Things/Minilang/explanation.md).

---

## What is MiniLang?

MiniLang is a complete programming language pipeline demonstrating every stage from source code to high-performance execution:

```text
Source (.ml) or Interactive REPL
  ↓
Lexer (tokens, operators, literals, kid-friendly keywords)
  ↓
Parser (recursive descent → AST with OOP, modules, and forgiving syntax)
  ↓
Compiler (AST → bytecode instructions + constant folding + jump backpatching)
  ↓
Stack-Based Virtual Machine (Call frames, memory cleanup, security limits)
  ↓
Rendering, Plotting & Graphics Engines (OpenGL 2D, Chart Plotter, Turtle Graphics)
```

---

## Highlights & Features

### 1. 🧒 Kid-Friendly & Beginner-Intuitive Syntax
MiniLang is designed so a **10-year-old child** can understand and write code right away, without dealing with annoying syntax errors:
- **Optional Semicolons**: Code lines don't need `;`. You can write naturally like Python or Scratch!
- **Auto Variable Assignment**: No need to type `let`. Just write `score = 100` or `name = "Alex"`.
- **Friendly Keywords**:
  - `say "Hello!"` or `show score` (or traditional `print(x)`)
  - `def calculateTotal(a, b)` or `fn calculateTotal(a, b)` or `function calculateTotal(a, b)`
  - `repeat 5 times { ... }` or `repeat 5 { ... }`
- **Natural English Operators**:
  - `and` instead of `&&`
  - `or` instead of `||`
  - `not` instead of `!`
  - `is` instead of `==`
  - Example: `if score >= 80 and isHappy { say "Great job!" }`
- **String Multiplication**:
  - `"Go! " * 3` produces `"Go! Go! Go! "`
  - `2 * "Na " + "Batman"` produces `"Na Na Batman"`
- **Intuitive Loops**:
  - `for x in range(10) { say x }`
  - `for item in ["cat", "dog", "bird"] { say item }`
  - `for ch in "hello" { say ch }`
  - Parentheses around `if` and `while` are completely optional!

---

### 2. 📊 Built-in Libraries & Packages

#### 📈 Scientific Chart Plotter (`import plot`)
Matplotlib-style 2D charting with auto-scaling, axes, grids, titles, series labels, and file export:
- `plotLine(y)` or `plotLine(x, y, label)`: Line graphs (e.g. math functions, exponential curves)
- `plotScatter(x, y, label)`: Scatter plots
- `plotBar(categories, values, label)`: Bar charts (e.g. sales, subject scores)
- `plotTitle(text)`, `plotXLabel(text)`, `plotYLabel(text)`: Configure labels
- `plotGrid(true/false)`: Toggle background grid
- `plotShow(w, h)`: Open chart window
- `plotSave(filePath, w, h)`: Export chart directly to PNG image (e.g. `output/quarterly_growth.png`)
- `plotClear()`: Reset canvas for the next plot

#### 🐢 Interactive Turtle Graphics (`import turtle`)
Logo/Python-style interactive visual geometry for kids:
- Movement: `forward(dist)` (alias `fd`), `backward(dist)` (alias `bk`)
- Turning: `turnRight(deg)` (alias `rt`), `turnLeft(deg)` (alias `lt`)
- Pen controls: `penUp()` (`pu`), `penDown()` (`pd`), `penColor(r, g, b)`, `penSize(pixels)`
- Shapes: `turtleCircle(radius)`, `turtleClear()`, `turtleSpeed(ms)`
- Saving art: `turtleSave(filePath)` saves drawings directly to PNG

#### ⚡ Fast Algorithm Standard Library (`import algo`)
Optimized built-in algorithms for data processing:
- `range(stop)` or `range(start, stop, step)`: Fast range array generator
- `sum(arr)`: Sum of numbers in array
- `mean(arr)`: Mathematical average
- `median(arr)`: Median value (handles both odd and even sized datasets)
- `sort(arr)` / `sort(arr, false)`: Fast ascending and descending sorting
- `reverse(arr)` / `reverse(str)`: Reversal of lists and strings
- `binarySearch(sortedArr, target)`: $O(\log n)$ binary search returning index or `-1`

#### 🎮 Hardware-Accelerated 2D/OpenGL Engine
- Zero external C/native dependencies; 100% pure Java double-buffered engine
- OpenGL immediate mode: `glBegin("TRIANGLES")`, `glVertex(x, y)`, `glEnd()`
- Window & loop control: `glInitWindow(w, h, title)`, `glIsOpen()`, `glUpdate()`, `glQuit()`
- Drawing primitives: `glClear`, `glColor`, `glRect`, `glFillRect`, `glCircle`, `glFillCircle`, `glLine`, `glText`
- Input polling: `glKeyDown("SPACE")`, `glMouseX()`, `glMouseY()`, `glMouseDown()`

---

### 3. 📦 Module & Package Import System
Organize projects cleanly with imports:
- **File Imports**: `import "path/to/helper.ml"` compiles and imports external functions and classes with circular-import prevention.
- **Built-in Packages**: `import algo`, `import plot`, `import turtle`.

---

### 4. 🧩 Object-Oriented Programming (Python-Style OOP)
- Class definitions: `class Player { ... }`
- Constructor: `fn init(name, score) { this.name = name; this.score = score; }`
- Dual receiver keyword: Both `this` and `self` work interchangeably (`this.x` or `self.x`)
- Dynamic fields and bound methods: `player.levelUp()`, dynamic properties

---

### 5. 🛡️ Security & Memory Safety
- **Call Stack Guard**: `MAX_CALL_STACK_DEPTH = 1000` prevents JVM stack overflows and infinite recursion attacks.
- **Automatic Frame Cleanup**: Local variables and arguments are immediately cleared upon return (`frame.locals.clear()`) to prevent memory leaks.

---

### 6. 🗄️ Native Dictionaries (HashMaps)
MiniLang 2.0 features first-class native dictionary / hashmap data structures:
- **Map Literals**: `{ "name": "Ada", "score": 98, "active": true }`
- **Indexing & Mutation**: `user["role"] = "Admin"`, `score = user["score"]`
- **Inspectors & Operations**:
  - `has(dict, "key")`: Check key existence (`true`/`false`)
  - `keys(dict)`: Returns dynamic array of all keys
  - `values(dict)`: Returns dynamic array of all values
  - `len(dict)`: Returns number of key-value pairs

---

### 7. 🛡️ User-Level Exception Handling (`try ... catch`)
Protect against runtime panics and gracefully recover from errors:
```minilang
try {
    let result = 100 / 0;
} catch (error) {
    print("Caught division error: " + error);
}
```
- Operates across nested function calls, unwinding activation frames cleanly.
- Catches division by zero, invalid index lookups, and file I/O errors.

---

### 8. 📂 File I/O Standard Library
Seamlessly read and write disk files:
- `writeFile("data.txt", "Hello World\n")`: Creates or overwrites file
- `readFile("data.txt")`: Reads complete file contents as a string
- `appendFile("data.txt", "Appended line\n")`: Appends data to existing file
- `fileExists("data.txt")`: Checks if a file exists on disk
- `deleteFile("data.txt")`: Safely deletes file from disk

---

### 9. 📜 Triple-Quoted Multi-line Strings
Write multi-line ASCII art, formatted text templates, or system banners:
```minilang
let banner = """
╔═════════════════════════════════════╗
║ MiniLang 2.0: Next-Gen Language     ║
╚═════════════════════════════════════╝
""";
print(banner);
```

---

### 10. 🌐 Interactive Web Studio & Visual VM Playground
MiniLang 2.0 comes with an embedded, zero-dependency Web Studio IDE:
- **Live Code Editor**: Line numbers, tab indentation, status bar, and preloaded example presets.
- **Visual Output Terminal**: Real-time script execution with sub-millisecond timing and memory safety status.
- **Bytecode VM Disassembler**: Interactive instruction breakdown showing opcodes, constant pool, and jump targets.
- **AST Explorer**: Inspect the parsed Abstract Syntax Tree directly in the browser.
- **Quick Reference Cheatsheet**: One-click snippet inserters for dictionaries, OOP, math, and graphics.
- **Launch Command**:
  ```bash
  java -jar target/minilang-1.0-SNAPSHOT.jar --web 8080
  ```
  Then open [http://localhost:8080](http://localhost:8080) in your browser!

---

## Complete Standard Library Reference

### Core & Math Builtins
| Function | Signature | Description |
| :--- | :--- | :--- |
| `print` / `say` / `show` | `(value)` | Outputs value to console with newline |
| `input` | `()` | Reads a string line from standard input |
| `len` | `(arr \| str)` | Returns length of array or string |
| `push` | `(arr, item)` | Appends item to dynamic array |
| `pop` | `(arr)` | Removes and returns last item of array |
| `str` | `(val)` | Converts value to string |
| `int` | `(val)` | Converts value to integer |
| `float` | `(val)` | Converts value to 64-bit float |
| `type` | `(val)` | Returns type name (`"integer"`, `"float"`, `"string"`, `"array"`, `"class"`, `"instance"`) |
| `sqrt` | `(num)` | Computes square root |
| `abs` | `(num)` | Computes absolute value |
| `min` | `(a, b)` | Returns minimum of two numbers |
| `max` | `(a, b)` | Returns maximum of two numbers |
| `floor` | `(num)` | Rounds down to nearest integer |
| `ceil` | `(num)` | Rounds up to nearest integer |
| `round` | `(num)` | Rounds to nearest integer |
| `pow` | `(base, exp)` | Computes $base^{exp}$ |
| `random` | `()` | Generates pseudo-random float in $[0.0, 1.0)$ |
| `sin` | `(rad)` | Sine of angle in radians |
| `cos` | `(rad)` | Cosine of angle in radians |
| `clock` | `()` | Current timestamp in seconds |

### Dictionaries & Maps
| Function | Signature | Description |
| :--- | :--- | :--- |
| `has` | `(map, key)` | Returns `true` if key exists in dictionary, `false` otherwise |
| `keys` | `(map)` | Returns dynamic array containing all keys in dictionary |
| `values` | `(map)` | Returns dynamic array containing all values in dictionary |
| `len` | `(map)` | Returns number of key-value pairs stored in map |

### File I/O Standard Library
| Function | Signature | Description |
| :--- | :--- | :--- |
| `readFile` | `(filePath)` | Reads complete file content as string (throws catchable exception on failure) |
| `writeFile` | `(filePath, content)` | Writes string to file, creating or overwriting |
| `appendFile` | `(filePath, content)` | Appends string to existing or new file |
| `fileExists` | `(filePath)` | Returns `true` if file exists on filesystem |
| `deleteFile` | `(filePath)` | Deletes file from filesystem; returns `true` on success |

### Fast Algorithms (`import algo`)
| Function | Signature | Description |
| :--- | :--- | :--- |
| `range` | `(stop)` or `(start, stop, step)` | Generates array sequence of numbers |
| `sum` | `(array)` | Returns sum of array elements |
| `mean` | `(array)` | Returns average of array elements |
| `median` | `(array)` | Returns median value of array |
| `sort` | `(array, [ascending=true])` | Returns sorted array |
| `reverse` | `(array \| str)` | Reverses array or string |
| `binarySearch` | `(sortedArray, target)` | Binary search returning index or -1 |

### Scientific Plotting (`import plot`)
| Function | Signature | Description |
| :--- | :--- | :--- |
| `plotLine` | `(yData)` or `(xData, yData, [label])` | Adds a continuous line series |
| `plotScatter` | `(xData, yData, [label])` | Adds a scatter point series |
| `plotBar` | `(categories, values, [label])` | Adds a categorical bar chart series |
| `plotTitle` | `(titleString)` | Sets the chart title |
| `plotXLabel` | `(labelString)` | Sets X-axis label |
| `plotYLabel` | `(labelString)` | Sets Y-axis label |
| `plotGrid` | `(enableBoolean)` | Toggles background gridlines |
| `plotShow` | `([width=700], [height=500])` | Displays interactive chart window |
| `plotSave` | `(filePath, [w=700], [h=500])` | Renders and saves chart directly to PNG file |
| `plotClear` | `()` | Clears all series and resets canvas |

### Turtle Graphics (`import turtle`)
| Function | Signature | Description |
| :--- | :--- | :--- |
| `turtleInit` | `([w=600], [h=600], [title])` | Initializes the turtle graphics window |
| `forward` / `fd` | `(distance)` | Moves turtle forward by distance |
| `backward` / `bk` | `(distance)` | Moves turtle backward by distance |
| `turnRight` / `rt` | `(degrees)` | Turns turtle clockwise by degrees |
| `turnLeft` / `lt` | `(degrees)` | Turns turtle counter-clockwise by degrees |
| `penUp` / `pu` | `()` | Lifts pen up (moves without drawing) |
| `penDown` / `pd` | `()` | Lowers pen down (draws on move) |
| `penColor` | `(r, g, b)` | Sets pen color (RGB $0-255$) |
| `penSize` | `(pixels)` | Sets pen line thickness |
| `turtleCircle` | `(radius)` | Draws a circle of specified radius |
| `turtleClear` | `()` | Clears turtle canvas and resets position |
| `turtleSpeed` | `(delayMs)` | Sets step animation delay (0 for instantaneous) |
| `turtleUpdate` | `()` | Refreshes turtle window |
| `turtleSave` | `(filePath)` | Exports turtle drawing directly to PNG file |

### OpenGL 2D Engine
| Function | Signature | Description |
| :--- | :--- | :--- |
| `glInitWindow` | `(w, h, title)` | Initializes double-buffered window |
| `glIsOpen` | `()` | Returns true if window is currently open |
| `glUpdate` | `()` | Swaps buffers and polls input events |
| `glClear` | `(r, g, b)` | Clears window background color |
| `glColor` | `(r, g, b)` | Sets current drawing color |
| `glRect` / `glFillRect` | `(x, y, w, h)` | Outlines or fills a rectangle |
| `glCircle` / `glFillCircle`| `(x, y, r)` | Outlines or fills a circle |
| `glLine` | `(x1, y1, x2, y2)` | Draws a line segment |
| `glText` | `(text, x, y)` | Draws text at position |
| `glBegin` | `("TRIANGLES" \| "LINES" \| "POINTS")` | Starts immediate mode vertex sequence |
| `glVertex` | `(x, y)` | Submits vertex in immediate mode |
| `glEnd` | `()` | Completes immediate mode batch |
| `glKeyDown` | `(keyName)` | Returns true if key (e.g. `"SPACE"`, `"UP"`, `"W"`) is held |
| `glMouseX` / `glMouseY` | `()` | Mouse coordinates |
| `glMouseDown` | `()` | Returns true if left mouse button is pressed |
| `glClose` | `()` | Closes graphics window |

---

## Quick Start

### 1. Build Executable JAR
```bash
mvn clean package
```

### 2. Interactive Web Studio IDE
```bash
java -jar target/minilang-1.0-SNAPSHOT.jar --web 8080
```
Open **[http://localhost:8080](http://localhost:8080)** in your browser for a dark glassmorphic playground with live bytecode and AST viewers!

### 3. Interactive REPL Shell
```bash
java -jar target/minilang-1.0-SNAPSHOT.jar --repl
```

Session example:
```minilang
minilang> score = 100
minilang> say "Cheer: " + ("Hip " * 2) + "Hurray!"
Cheer: Hip Hip Hurray!
minilang> for i in range(1, 4) { say "Step " + str(i) }
Step 1
Step 2
Step 3
minilang> .exit
```

### 4. Run Included Examples

```bash
# 1. Advanced Capabilities Demo (Dictionaries, File I/O, Try/Catch, Multi-line Strings)
java -jar target/minilang-1.0-SNAPSHOT.jar examples/advanced_demo.ml

# 2. Kid-Friendly Demo (simple syntax, repeat loops, natural English conditions)
java -jar target/minilang-1.0-SNAPSHOT.jar examples/kid_friendly_demo.ml

# 3. Fast Algorithms & Statistics Demo (range, sort, mean, median, binarySearch)
java -jar target/minilang-1.0-SNAPSHOT.jar examples/algorithms_demo.ml

# 4. Scientific Chart Plotter Demo (line, scatter, bar charts saved to PNG)
java -jar target/minilang-1.0-SNAPSHOT.jar examples/plot_demo.ml

# 5. Interactive Turtle Graphics Demo (geometric star spiral saved to PNG)
java -jar target/minilang-1.0-SNAPSHOT.jar examples/turtle_demo.ml

# 6. Modular Import System Demo
java -jar target/minilang-1.0-SNAPSHOT.jar examples/import_demo.ml

# 7. Python-style Object-Oriented Programming Demo
java -jar target/minilang-1.0-SNAPSHOT.jar examples/oop_demo.ml

# 8. Hardware-accelerated OpenGL / Graphics Window Demo
java -jar target/minilang-1.0-SNAPSHOT.jar examples/graphics_demo.ml
```

### 5. Compiler Inspection & Debug Modes

```bash
# Token stream
java -jar target/minilang-1.0-SNAPSHOT.jar --tokens examples/advanced_demo.ml

# AST hierarchy
java -jar target/minilang-1.0-SNAPSHOT.jar --ast examples/advanced_demo.ml

# Bytecode disassembly
java -jar target/minilang-1.0-SNAPSHOT.jar --bytecode examples/advanced_demo.ml
```

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
│   ├── import_demo.ml         (Demonstrates importing math_helper.ml and algo)
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
        ├── AdvancedFeaturesTest.java (Dictionaries, File I/O, Try/Catch, Multi-line)
        ├── KidFriendlyAndLibrariesTest.java (Kid syntax, Repeat, Algo, Plot, Turtle)
        ├── CompilerTest.java
        ├── Features2Test.java
        ├── LexerTest.java
        ├── OopAndSecurityTest.java
        ├── ParserTest.java
        └── VMTest.java
```

---

## Automated Test Suite

MiniLang includes **49 automated unit tests** across all language layers:

```bash
mvn test
```

Test suites cover:
- `AdvancedFeaturesTest`: Dictionaries (maps), key lookup/mutation, File I/O (`writeFile`, `readFile`, `appendFile`, `fileExists`, `deleteFile`), `try ... catch` exception unwinding, and multi-line strings.
- `KidFriendlyAndLibrariesTest`: Optional semicolons, auto-assignment, English operators, `repeat`, `for-in`, string multiplication, `algo`, `plot`, `turtle`, and `import`.
- `LexerTest`: Tokenization of keywords, numbers, floats, strings, comments.
- `ParserTest`: Expressions, statements, precedence, calls, arrays, loops, classes.
- `CompilerTest`: Unoptimized & optimized bytecode, constant folding, backpatching.
- `VMTest`: Arithmetic, comparisons, dynamic arrays, indexing, loops, recursion, builtins.
- `Features2Test`: Floating point math, array push/pop, break/continue, math library.
- `OopAndSecurityTest`: Classes, `init`, methods, `this`/`self`, dynamic fields, recursion stack overflow limits, memory disposal, OpenGL builtins.

---

## License

Educational / MIT.
