// MiniLang 2.0 Web Studio Application Logic

const EXAMPLES = {
    kid_friendly: `// ========================================================
// MiniLang 2.0: Kid-Friendly English Syntax
// Clear, natural language keywords and friendly loops
// ========================================================

let score = 100;
show "Initial Score: " + str(score);

// Easy natural arithmetic
score = score + 25;
show "After adding 25: " + str(score);

score = score - 10;
show "After subtracting 10: " + str(score);

score = score * 2;
show "After doubling: " + str(score);

score = score / 5;
show "After dividing by 5: " + str(score);

print("\n--- Kid-Friendly Repeat Loops ---");
let step = 1;
repeat 4 times {
    say "Loop iteration #" + str(step) + ": MiniLang is simple and fast.";
    step = step + 1;
}
`,

    oop_classes: `// ========================================================
// MiniLang 2.0: Object-Oriented Programming
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

// Recursive Fibonacci with dynamic array memoization
fn fib(n) {
    if (n <= 1) return n;
    return fib(n - 1) + fib(n - 2);
}

print("Computing Fibonacci values:");
let i = 0;
while (i <= 10) {
    print("fib(" + str(i) + ") = " + str(fib(i)));
    i = i + 1;
}

// Array statistics and sorting
let nums = [42, 17, 88, 3, 99, 12, 55];
print("\nOriginal array: " + str(nums));
print("Array length:   " + str(len(nums)));
print("Sorted array:   " + str(sort(nums)));
print("Max value:      " + str(max(nums)));
print("Min value:      " + str(min(nums)));
print("Mean value:     " + str(mean(nums)));
print("Median value:   " + str(median(nums)));
`,

    try_catch: `// ========================================================
// MiniLang 2.0: Exception Handling (Try / Catch)
// ========================================================

print("=== Safe Exception Handling Demonstration ===");

// 1. Catching Division by Zero
try {
    print("Executing division 100 / 0...");
    let result = 100 / 0;
    print("This will not execute.");
} catch (err) {
    print("Handled runtime error: " + err);
}

// 2. Function with internal recovery
fn safeDivide(a, b) {
    try {
        return a / b;
    } catch (e) {
        print("safeDivide caught: " + e);
        return 0;
    }
}

print("\nsafeDivide(50, 5) = " + str(safeDivide(50, 5)));
print("safeDivide(50, 0) = " + str(safeDivide(50, 0)));
print("\nSystem state: Stable and running normally.");
`,

    multiline_str: `// ========================================================
// MiniLang 2.0: Triple-Quoted Multi-line Raw Strings
// ========================================================

let banner = """
+---------------------------------------------+
| MiniLang 2.0 Professional Workbench         |
| High-performance bytecode compiler and VM   |
+---------------------------------------------+
""";

print(banner);

let info = """
Core Features:
  - Bytecode Virtual Machine with zero native dependencies
  - Object-Oriented Architecture (Classes & Methods)
  - Kid-Friendly Natural English Syntax
  - Safe Memory Management with Call Stack Limits
  - 2D Drawing, Chart Plotting, and Fast Algorithms
""";

print(info);
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

// Initialize with kid-friendly example
editor.value = EXAMPLES.kid_friendly;
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

// Tab key indentation
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
