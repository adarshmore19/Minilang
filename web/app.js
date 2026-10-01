// MiniLang Web Studio Application Logic

const DEFAULT_SCRIPT = `// MiniLang Language Workbench
// Write your code here and press Run (Ctrl+Enter)

let score = 100;
show "Initial Score: " + str(score);

// Kid-friendly natural syntax
score = score + 25;
show "After adding 25: " + str(score);

score = score * 2;
show "After doubling: " + str(score);

// Repeat loops
print("\nExecuting repeat loop:");
let step = 1;
repeat 3 times {
    say "Loop step " + str(step) + ": MiniLang is simple and fast.";
    step = step + 1;
}

// Functions & Safe math
fn calculateBonus(points) {
    return points * 1.5;
}

let bonus = calculateBonus(score);
print("\nCalculated bonus: " + str(bonus));
`;

// DOM Elements
const editor = document.getElementById('code-editor');
const highlightLayer = document.getElementById('highlight-layer');
const highlightCode = document.getElementById('highlight-code');
const lineNumbers = document.getElementById('line-numbers');
const runBtn = document.getElementById('run-btn');
const bytecodeBtn = document.getElementById('bytecode-btn');
const astBtn = document.getElementById('ast-btn');
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

// Theme Toggle Elements
const themeToggleBtn = document.getElementById('theme-toggle-btn');
const themeIconMoon = document.getElementById('theme-icon-moon');
const themeIconSun = document.getElementById('theme-icon-sun');

// Documentation Search Elements
const searchInput = document.getElementById('syntax-search-input');
const searchCount = document.getElementById('search-count');
const syntaxCards = document.querySelectorAll('.ref-card');

if (searchCount && syntaxCards) {
    searchCount.textContent = `${syntaxCards.length} topics`;
}

// Initialize Editor with Default Script
editor.value = DEFAULT_SCRIPT;
updateEditorView();

// -----------------------------------------------------------------------------
// 1. Syntax Highlighting Engine
// -----------------------------------------------------------------------------
function escapeHtml(str) {
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

function renderSyntaxHighlighting(text) {
    const tokenRegex = /(\/\/[^\n]*|\/\*[\s\S]*?\*\/)|("""[\s\S]*?"""|"(?:\\.|[^"\\])*")|(\b(?:let|fn|def|class|if|else|while|repeat|times|for|in|return|try|catch|import|say|show|print|this|self)\b)|(\b(?:and|or|not|is|true|false|null)\b)|(\b\d+(?:\.\d+)?\b)|(\b(?:range|sort|mean|median|sum|reverse|binarySearch|has|keys|values|len|push|pop|sqrt|pow|abs|round|floor|ceil|sin|cos|random|clock|readFile|writeFile|appendFile|fileExists|deleteFile)\b)/g;

    let result = "";
    let lastIndex = 0;
    let match;

    while ((match = tokenRegex.exec(text)) !== null) {
        result += escapeHtml(text.slice(lastIndex, match.index));

        if (match[1]) {
            // Comment
            result += `<span class="token-comment">${escapeHtml(match[1])}</span>`;
        } else if (match[2]) {
            // String (single or multi-line)
            result += `<span class="token-string">${escapeHtml(match[2])}</span>`;
        } else if (match[3]) {
            // Keyword
            result += `<span class="token-keyword">${escapeHtml(match[3])}</span>`;
        } else if (match[4]) {
            // Operator / Boolean
            result += `<span class="token-operator">${escapeHtml(match[4])}</span>`;
        } else if (match[5]) {
            // Number
            result += `<span class="token-number">${escapeHtml(match[5])}</span>`;
        } else if (match[6]) {
            // Standard Builtin
            result += `<span class="token-builtin">${escapeHtml(match[6])}</span>`;
        }
        lastIndex = tokenRegex.lastIndex;
    }
    result += escapeHtml(text.slice(lastIndex));
    if (text.endsWith('\n')) {
        result += ' ';
    }
    return result;
}

function updateEditorView() {
    updateLineNumbers();
    updateStats();
    if (highlightCode) {
        highlightCode.innerHTML = renderSyntaxHighlighting(editor.value);
    }
}

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

// Input and Scroll Synchronization
editor.addEventListener('input', updateEditorView);
editor.addEventListener('click', updateStats);
editor.addEventListener('keyup', updateStats);

editor.addEventListener('scroll', () => {
    lineNumbers.scrollTop = editor.scrollTop;
    if (highlightLayer) {
        highlightLayer.scrollTop = editor.scrollTop;
        highlightLayer.scrollLeft = editor.scrollLeft;
    }
});

// Tab indentation and shortcut handling
editor.addEventListener('keydown', (e) => {
    if (e.key === 'Tab') {
        e.preventDefault();
        const start = editor.selectionStart;
        const end = editor.selectionEnd;
        editor.value = editor.value.substring(0, start) + '    ' + editor.value.substring(end);
        editor.selectionStart = editor.selectionEnd = start + 4;
        updateEditorView();
    } else if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
        e.preventDefault();
        runCode();
    }
});

// -----------------------------------------------------------------------------
// 2. Calming Dark Theme Toggle
// -----------------------------------------------------------------------------
function applyTheme(isDark) {
    if (isDark) {
        document.body.classList.add('dark-theme');
        if (themeIconMoon) themeIconMoon.style.display = 'none';
        if (themeIconSun) themeIconSun.style.display = 'inline-block';
    } else {
        document.body.classList.remove('dark-theme');
        if (themeIconMoon) themeIconMoon.style.display = 'inline-block';
        if (themeIconSun) themeIconSun.style.display = 'none';
    }
}

// Load saved theme preference
const savedTheme = localStorage.getItem('minilang-theme');
if (savedTheme === 'dark') {
    applyTheme(true);
} else {
    applyTheme(false);
}

themeToggleBtn.addEventListener('click', () => {
    const isDarkNow = document.body.classList.contains('dark-theme');
    const nextDark = !isDarkNow;
    applyTheme(nextDark);
    localStorage.setItem('minilang-theme', nextDark ? 'dark' : 'light');
});

// -----------------------------------------------------------------------------
// 3. Searchable Syntax Documentation
// -----------------------------------------------------------------------------
if (searchInput) {
    searchInput.addEventListener('input', () => {
        const query = searchInput.value.toLowerCase().trim();
        let visibleCount = 0;

        syntaxCards.forEach(card => {
            const keywords = (card.getAttribute('data-keywords') || '').toLowerCase();
            const text = card.textContent.toLowerCase();

            if (!query || keywords.includes(query) || text.includes(query)) {
                card.style.display = 'flex';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        if (searchCount) {
            searchCount.textContent = `${visibleCount} of ${syntaxCards.length} topics`;
        }
    });
}

// "Insert into Compiler" Buttons
document.querySelectorAll('.insert-snippet-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const snippet = btn.getAttribute('data-snippet');
        const pos = editor.selectionStart;
        editor.value = editor.value.substring(0, pos) + '\n' + snippet + '\n' + editor.value.substring(pos);
        updateEditorView();
        switchTab('console-view');
        editor.focus();
    });
});

// -----------------------------------------------------------------------------
// 4. Tab Navigation
// -----------------------------------------------------------------------------
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

// -----------------------------------------------------------------------------
// 5. Execution REST API Calls
// -----------------------------------------------------------------------------
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

// Action Button Listeners
runBtn.addEventListener('click', runCode);
bytecodeBtn.addEventListener('click', showBytecode);
astBtn.addEventListener('click', showAst);

formatBtn.addEventListener('click', () => {
    editor.value = '';
    updateEditorView();
    editor.focus();
});

clearConsoleBtn.addEventListener('click', () => {
    consoleOutput.textContent = '';
    outputBadge.textContent = '0';
    metricStatus.textContent = 'Idle';
    metricStatus.className = 'metric-pill';
});

// Clipboard Copy Listeners
document.getElementById('copy-bytecode-btn').addEventListener('click', () => {
    navigator.clipboard.writeText(bytecodeOutput.textContent);
});

document.getElementById('copy-ast-btn').addEventListener('click', () => {
    navigator.clipboard.writeText(astOutput.textContent);
});
