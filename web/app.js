// MiniLang 2.0 Web Studio Application Logic

const EXAMPLES = {
    dicts_fileio: `// ========================================================
// MiniLang 2.0: Dictionaries (HashMaps) & File I/O Built-ins
// ========================================================

print("--- 1. Native Dictionaries / HashMaps ---");
let user = {
    "name": "Alex",
    "role": "Lead Architect",
    "level": 42
};

// Accessing and mutating dictionary values
print("User name: " + user["name"]);
print("User level: " + str(user["level"]));

user["level"] = user["level"] + 1;
user["verified"] = true;
print("Updated user: " + str(user));

// Built-in map inspectors
print("Has 'role' key? " + str(has(user, "role")));
print("Has 'salary' key? " + str(has(user, "salary")));
print("Dictionary keys: " + str(keys(user)));
print("Dictionary values: " + str(values(user)));
print("User map size: " + str(len(user)));

print("\n--- 2. File I/O Standard Library ---");
let filename = "sandbox_greeting.txt";

// Write file
writeFile(filename, "Hello from MiniLang 2.0!\nCrafted with high performance & safe memory.\n");
print("File written. Exists? " + str(fileExists(filename)));

// Append to file
appendFile(filename, "Appending an extra log line.\n");

// Read file content
let content = readFile(filename);
print("File contents:\n" + content);

// Cleanup
deleteFile(filename);
print("Deleted file. Exists now? " + str(fileExists(filename)));
`,

    try_catch: `// ========================================================
// MiniLang 2.0: Safe Exception Handling (Try / Catch)
// ========================================================

print("=== Safe Exception Handling Demonstration ===");

// 1. Catching Division by Zero
try {
    print("Attempting dangerous division 100 / 0...");
    let result = 100 / 0;
    print("This will not print!");
} catch (err) {
    print(">>> Caught expected error: " + err);
}

// 2. Catching File Not Found
try {
    print("\nAttempting to read a non-existent file...");
    let secret = readFile("non_existent_vault.txt");
} catch (e) {
    print(">>> Handled File I/O exception safely: " + e);
}

// 3. Normal execution flow continues smoothly!
print("\nSystem state: Stable and running smoothly!");
`,

    multiline_str: `// ========================================================
// MiniLang 2.0: Triple-Quoted Multi-line Raw Strings
// ========================================================

let banner = """
   __  __ _       _ _                    ___    ___  
  |  \\/  (_)_ __ (_) |   __ _ _ __   __ _|__ \\  / _ \\ 
  | |\\/| | | '_ \\| | |  / _\` | '_ \\ / _\` | / / | | | |
  | |  | | | | | | | | | (_| | | | | (_| |/ /_ | |_| |
  |_|  |_|_|_| |_|_|_|  \\__,_|_| |_|\\__, |____(_)___/ 
                                    |___/             
""";

print(banner);

let info = """
MiniLang 2.0 Features:
  * Bytecode Virtual Machine with zero external runtime deps
  * Pythonic Object-Oriented Programming (Classes & Methods)
  * Kid-Friendly Natural English Syntax
  * Native Dictionaries, Dynamic Arrays, & Safe Memory
  * Rich 2D Drawing, Plotting & Interactive Web Studio
""";

print(info);
`,

    kid_friendly: `// ========================================================
// MiniLang 2.0: Kid-Friendly English Syntax
// Clear, natural language that anyone can understand!
// ========================================================

let score = 100;
show "Initial Score: " + str(score);

// Friendly natural math verbs
add 25 to score;
show "After add 25: " + str(score);

subtract 10 from score;
show "After subtract 10: " + str(score);

multiply score by 2;
show "After doubling: " + str(score);

divide score by 5;
show "After dividing by 5: " + str(score);

print("\n--- Kid-Friendly Repeat Loops ---");
let step = 1;
repeat 4 times {
    say "Loop iteration #" + str(step) + ": MiniLang is easy!";
    add 1 to step;
}
`,

    oop_classes: `// ========================================================
// MiniLang 2.0: Pythonic Object-Oriented Programming
// Classes, Constructors, 'this' / 'self', and Methods
// ========================================================

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

    show() {
        print("(" + str(round(this.x)) + ", " + str(round(this.y)) + ")");
    }
}

let v1 = Vector2D(3.0, 4.0);
print("Vector v1 magnitude (expected 5.0): " + str(v1.magnitude()));

let v2 = Vector2D(7.0, 6.0);
let v3 = v1.add(v2);

print("v3 = v1 + v2:");
v3.show();
`,

    algorithms: `// ========================================================
// MiniLang 2.0: High-Speed Algorithms & Dynamic Arrays
// ========================================================

// 1. Recursive Fibonacci with Memoization
let memo = {};

fn fib(n) {
    if (n <= 1) return n;
    let key = str(n);
    if (has(memo, key)) {
        return memo[key];
    }
    let res = fib(n - 1) + fib(n - 2);
    memo[key] = res;
    return res;
}

print("Computing Fibonacci sequence with memoization:");
let i = 0;
while (i <= 15) {
    print("fib(" + str(i) + ") = " + str(fib(i)));
    i = i + 1;
}

// 2. High-speed array operations
let nums = [42, 17, 88, 3, 99, 12, 55];
print("\nOriginal array: " + str(nums));
print("Array length: " + str(len(nums)));
print("Max value: " + str(max(nums)));
print("Min value: " + str(min(nums)));
`,

    turtle_art: `// ========================================================
// MiniLang 2.0: ASCII Canvas & Shape Rendering
// ========================================================

fn drawBox(w, h) {
    let y = 0;
    while (y < h) {
        let line = "";
        let x = 0;
        while (x < w) {
            if (y == 0 || y == h - 1) {
                line = line + "#";
            } else {
                if (x == 0 || x == w - 1) {
                    line = line + "#";
                } else {
                    line = line + " ";
                }
            }
            x = x + 1;
        }
        print(line);
        y = y + 1;
    }
}

print("Drawing 16x8 ASCII frame:");
drawBox(16, 8);
`
};

// DOM Elements
const editor = document.getElementById('code-editor');
const lineNumbers = document.getElementById('line-numbers');
const runBtn = document.getElementById('run-btn');
const bytecodeBtn = document.getElementById('bytecode-btn');
const astBtn = document.getElementById('ast-btn');
const exampleSelect = document.getElementById('example-select');
const consoleOutput = document.getElementById('console-output');
const bytecodeOutput = document.getElementById('bytecode-output');
const astOutput = document.getElementById('ast-output');
const metricStatus = document.getElementById('metric-status');
const metricTime = document.getElementById('metric-time');
const outputBadge = document.getElementById('output-badge');
const editorStatus = document.getElementById('editor-status');
const cursorPos = document.getElementById('cursor-pos');
const charCount = document.getElementById('char-count');
const formatBtn = document.getElementById('format-btn');
const clearConsoleBtn = document.getElementById('clear-console-btn');

// Initialize with first example
editor.value = EXAMPLES.dicts_fileio;
updateLineNumbers();
updateStats();

// Synchronize line numbers and cursor tracker
function updateLineNumbers() {
    const lines = editor.value.split('\n').length;
    let numbersText = '';
    for (let i = 1; i <= lines; i++) {
        numbersText += i + '\n';
    }
    lineNumbers.textContent = numbersText;
}

function updateStats() {
    const val = editor.value;
    charCount.textContent = val.length + ' chars';
    const selStart = editor.selectionStart;
    const linesUpToCursor = val.substring(0, selStart).split('\n');
    const row = linesUpToCursor.length;
    const col = linesUpToCursor[linesUpToCursor.length - 1].length + 1;
    cursorPos.textContent = `Ln ${row}, Col ${col}`;
}

editor.addEventListener('input', () => {
    updateLineNumbers();
    updateStats();
});

editor.addEventListener('click', updateStats);
editor.addEventListener('keyup', updateStats);

editor.addEventListener('scroll', () => {
    lineNumbers.scrollTop = editor.scrollTop;
});

// Tab key indentation and auto-closing quotes/brackets
editor.addEventListener('keydown', (e) => {
    if (e.key === 'Tab') {
        e.preventDefault();
        const start = editor.selectionStart;
        const end = editor.selectionEnd;
        editor.value = editor.value.substring(0, start) + '    ' + editor.value.substring(end);
        editor.selectionStart = editor.selectionEnd = start + 4;
        updateLineNumbers();
        updateStats();
    } else if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
        e.preventDefault();
        runCode();
    }
});

// Load preset examples
exampleSelect.addEventListener('change', () => {
    const key = exampleSelect.value;
    if (EXAMPLES[key]) {
        editor.value = EXAMPLES[key];
        updateLineNumbers();
        updateStats();
        editor.scrollTop = 0;
    }
});

// Tab bar handling
const tabButtons = document.querySelectorAll('.tab-btn');
const tabPanels = document.querySelectorAll('.tab-panel');

tabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
        const targetTab = btn.getAttribute('data-tab');
        tabButtons.forEach(b => b.classList.remove('active'));
        tabPanels.forEach(p => p.classList.remove('active'));

        btn.classList.add('active');
        const activePanel = document.getElementById(targetTab);
        if (activePanel) activePanel.classList.add('active');
    });
});

function switchTab(tabId) {
    const btn = document.querySelector(`[data-tab="${tabId}"]`);
    if (btn) btn.click();
}

// Quick reference snippet inserters
document.querySelectorAll('.insert-snippet-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const snippet = btn.getAttribute('data-snippet');
        const pos = editor.selectionStart;
        editor.value = editor.value.substring(0, pos) + '\n' + snippet + '\n' + editor.value.substring(pos);
        updateLineNumbers();
        updateStats();
        switchTab('console-view');
        editor.focus();
    });
});

// API Calls
async function runCode() {
    const code = editor.value;
    editorStatus.textContent = "Executing...";
    metricStatus.textContent = "Running";
    metricStatus.className = "metric-pill";

    try {
        const response = await fetch('/api/run', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: code })
        });

        const data = await response.json();
        editorStatus.textContent = "Ready";
        metricTime.textContent = (data.timeMs || 0) + " ms";

        if (data.success) {
            metricStatus.textContent = "Success";
            metricStatus.className = "metric-pill success";
            consoleOutput.className = "terminal-view terminal-success";
            consoleOutput.textContent = data.output || "(Program exited cleanly with no output)";
            outputBadge.textContent = data.output ? data.output.split('\n').filter(Boolean).length : "0";
            if (data.bytecode) {
                bytecodeOutput.textContent = data.bytecode;
            }
        } else {
            metricStatus.textContent = "Error";
            metricStatus.className = "metric-pill error";
            consoleOutput.className = "terminal-view terminal-error";
            consoleOutput.textContent = "Runtime / Compile Error:\n" + (data.error || "Unknown error") + 
                (data.output ? "\n\nStandard Output before failure:\n" + data.output : "");
            outputBadge.textContent = "!";
        }
        switchTab('console-view');
    } catch (err) {
        editorStatus.textContent = "Offline";
        metricStatus.textContent = "Failed";
        metricStatus.className = "metric-pill error";
        consoleOutput.className = "terminal-view terminal-error";
        consoleOutput.textContent = "Connection error to MiniLang server: " + err.message;
    }
}

async function showBytecode() {
    const code = editor.value;
    try {
        const response = await fetch('/api/disassemble', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: code })
        });
        const data = await response.json();
        if (data.success) {
            bytecodeOutput.textContent = data.bytecode;
        } else {
            bytecodeOutput.textContent = "Compilation Error:\n" + data.error;
        }
        switchTab('bytecode-view');
    } catch (err) {
        bytecodeOutput.textContent = "Failed to fetch bytecode: " + err.message;
    }
}

async function showAst() {
    const code = editor.value;
    try {
        const response = await fetch('/api/ast', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: code })
        });
        const data = await response.json();
        if (data.success) {
            astOutput.textContent = data.ast;
        } else {
            astOutput.textContent = "Parser Error:\n" + data.error;
        }
        switchTab('ast-view');
    } catch (err) {
        astOutput.textContent = "Failed to fetch AST: " + err.message;
    }
}

// Action Buttons
runBtn.addEventListener('click', runCode);
bytecodeBtn.addEventListener('click', showBytecode);
astBtn.addEventListener('click', showAst);

formatBtn.addEventListener('click', () => {
    editor.value = '';
    updateLineNumbers();
    updateStats();
    editor.focus();
});

clearConsoleBtn.addEventListener('click', () => {
    consoleOutput.textContent = '';
    outputBadge.textContent = '0';
    metricStatus.textContent = 'Idle';
    metricStatus.className = 'metric-pill';
});

// Copy buttons
document.getElementById('copy-bytecode-btn').addEventListener('click', () => {
    navigator.clipboard.writeText(bytecodeOutput.textContent);
});

document.getElementById('copy-ast-btn').addEventListener('click', () => {
    navigator.clipboard.writeText(astOutput.textContent);
});
