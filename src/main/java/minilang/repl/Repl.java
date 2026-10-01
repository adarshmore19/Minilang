package minilang.repl;

import minilang.compiler.Compiler;
import minilang.compiler.Program;
import minilang.lexer.Lexer;
import minilang.lexer.TokenType;
import minilang.parser.ExpressionStmt;
import minilang.parser.Parser;
import minilang.parser.Stmt;
import minilang.vm.NullValue;
import minilang.vm.VM;
import minilang.vm.Value;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive Read-Eval-Print Loop for MiniLang.
 */
public class Repl {

    private final List<String> history = new ArrayList<>();
    private boolean showBytecode = false;

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("==================================================");
        System.out.println(" MiniLang 2.0 Interactive REPL");
        System.out.println(" Type statements or expressions. Functions & variables persist.");
        System.out.println(" Commands: .help, .clear, .bytecode, .exit");
        System.out.println("==================================================");

        StringBuilder buffer = new StringBuilder();

        while (true) {
            String prompt = buffer.length() == 0 ? "minilang> " : "......> ";
            System.out.print(prompt);
            System.out.flush();

            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine();
            String trimmed = line.trim();

            if (buffer.length() == 0) {
                if (trimmed.equals(".exit") || trimmed.equals(".quit")) {
                    System.out.println("Goodbye!");
                    break;
                }
                if (trimmed.equals(".clear")) {
                    history.clear();
                    System.out.println("Environment cleared.");
                    continue;
                }
                if (trimmed.equals(".bytecode")) {
                    showBytecode = !showBytecode;
                    System.out.println("Bytecode display: " + (showBytecode ? "ON" : "OFF"));
                    continue;
                }
                if (trimmed.equals(".help")) {
                    printHelp();
                    continue;
                }
                if (trimmed.isEmpty()) {
                    continue;
                }
            }

            buffer.append(line).append("\n");

            if (!isComplete(buffer.toString())) {
                continue;
            }

            String input = buffer.toString().trim();
            buffer.setLength(0);

            eval(input);
        }
    }

    private void eval(String input) {
        // Try evaluating as an expression first (auto-print result)
        boolean isStatement = input.startsWith("let ") || input.startsWith("fn ")
                || input.startsWith("if ") || input.startsWith("if(")
                || input.startsWith("while ") || input.startsWith("while(")
                || input.startsWith("for ") || input.startsWith("for(")
                || input.startsWith("return ");

        if (!isStatement && !input.endsWith(";")) {
            // Test if wrapping in print() works as an expression
            String candidateExpr = input.endsWith(";") ? input : input + ";";
            try {
                String sourceWithPrint = buildSource("print(" + input + ");");
                ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(outBytes, true, StandardCharsets.UTF_8);

                List<Stmt> stmts = new Parser(new Lexer(sourceWithPrint)).parse();
                Program program = new Compiler(true).compile(stmts);
                if (showBytecode) {
                    System.out.print(program.disassemble());
                }
                VM vm = new VM(program, ps);
                vm.run();

                String out = outBytes.toString(StandardCharsets.UTF_8).trim();
                if (!out.isEmpty()) {
                    System.out.println(out);
                }
                return;
            } catch (Exception ignored) {
                // Fall through to statement execution
            }
        }

        // Execute as statement or general code
        try {
            String candidate = input.endsWith(";") || input.endsWith("}") ? input : input + ";";
            String fullSource = buildSource(candidate);

            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(outBytes, true, StandardCharsets.UTF_8);

            List<Stmt> stmts = new Parser(new Lexer(fullSource)).parse();
            Program program = new Compiler(true).compile(stmts);
            if (showBytecode) {
                System.out.print(program.disassemble());
            }

            VM vm = new VM(program, ps);
            vm.run();

            String out = outBytes.toString(StandardCharsets.UTF_8).trim();
            if (!out.isEmpty()) {
                System.out.println(out);
            }

            // Successfully executed: persist declaration to session history
            history.add(candidate);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private String buildSource(String nextSnippet) {
        StringBuilder sb = new StringBuilder();
        for (String prev : history) {
            sb.append(prev).append("\n");
        }
        sb.append(nextSnippet).append("\n");
        return sb.toString();
    }

    private boolean isComplete(String text) {
        int braces = 0;
        int brackets = 0;
        int parens = 0;
        boolean inString = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' && (i == 0 || text.charAt(i - 1) != '\\')) {
                inString = !inString;
                continue;
            }
            if (inString) continue;

            if (c == '{') braces++;
            else if (c == '}') braces--;
            else if (c == '[') brackets++;
            else if (c == ']') brackets--;
            else if (c == '(') parens++;
            else if (c == ')') parens--;
        }

        return braces <= 0 && brackets <= 0 && parens <= 0;
    }

    private void printHelp() {
        System.out.println("""
            --- MiniLang 2.0 Commands & Syntax ---
            Commands:
              .help       Show this help message
              .clear      Reset environment state
              .bytecode   Toggle bytecode disassembly
              .exit       Exit REPL

            Syntax & Types:
              Variables:    let x = 10; let f = 3.14; let s = "text";
              Arrays:       let arr = [1, 2, 3]; arr[0] = 99; print(arr);
              Control flow: if (x > 5) { ... } else { ... }
              Loops:        while (x < 10) { ... break; continue; }
                            for (let i = 0; i < 5; i = i + 1) { ... }
              Functions:    fn add(a, b) { return a + b; }
              Builtins:     len(x), push(arr, v), pop(arr), clock(), input()
                            sqrt(x), abs(x), min(a,b), max(a,b), floor(x), ceil(x), round(x), pow(b,e), random()
                            sin(x), cos(x), str(v), int(v), float(v), type(v)
            """);
    }
}
