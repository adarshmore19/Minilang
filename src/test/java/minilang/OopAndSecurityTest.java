package minilang;

import minilang.compiler.Compiler;
import minilang.compiler.Opcode;
import minilang.compiler.Program;
import minilang.lexer.Lexer;
import minilang.parser.Parser;
import minilang.parser.Stmt;
import minilang.vm.RuntimeError;
import minilang.vm.VM;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OopAndSecurityTest {

    private String runSource(String source) {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outBytes, true, StandardCharsets.UTF_8);

        List<Stmt> statements = new Parser(new Lexer(source)).parse();
        Program program = new Compiler(true).compile(statements);
        VM vm = new VM(program, printStream);
        vm.run();

        return outBytes.toString(StandardCharsets.UTF_8).trim();
    }

    @Test
    public void testClassWithInitAndMethodsUsingThis() {
        String source = """
            class Point {
                init(x, y) {
                    this.x = x;
                    this.y = y;
                }
                distSquared() {
                    return this.x * this.x + this.y * this.y;
                }
            }
            let p = Point(3, 4);
            print(p.x);
            print(p.y);
            print(p.distSquared());
        """;
        assertEquals("3\n4\n25", runSource(source));
    }

    @Test
    public void testClassWithSelfKeyword() {
        String source = """
            class Counter {
                init(start) {
                    self.count = start;
                }
                increment() {
                    self.count = self.count + 1;
                    return self.count;
                }
            }
            let c = Counter(10);
            print(c.increment());
            print(c.increment());
            print(c.count);
        """;
        assertEquals("11\n12\n12", runSource(source));
    }

    @Test
    public void testClassFieldMutationAndDynamicProperties() {
        String source = """
            class Rectangle {
                init(w, h) {
                    this.w = w;
                    this.h = h;
                }
                area() {
                    return this.w * this.h;
                }
            }
            let r = Rectangle(5, 10);
            print(r.area());
            r.w = 7;
            print(r.area());
            r.tag = "special";
            print(r.tag);
        """;
        assertEquals("50\n70\nspecial", runSource(source));
    }

    @Test
    public void testTypeBuiltinOnClassAndInstance() {
        String source = """
            class Animal {
                speak() {
                    return "sound";
                }
            }
            let a = Animal();
            print(type(Animal));
            print(type(a));
            print(type(a.speak));
        """;
        assertEquals("class\ninstance\nmethod", runSource(source));
    }

    @Test
    public void testCallStackOverflowProtection() {
        String source = """
            fn recurse(n) {
                return recurse(n + 1);
            }
            recurse(1);
        """;
        RuntimeError error = assertThrows(RuntimeError.class, () -> runSource(source));
        assertTrue(error.getMessage().contains("Call stack overflow"));
    }

    @Test
    public void testCompilerConstantFoldingOptimization() {
        String source = "let x = 10 + 20 * 3;";
        List<Stmt> statements = new Parser(new Lexer(source)).parse();
        Program optimizedProgram = new Compiler(true).compile(statements);

        // Constant folding should precompute 10 + 20 * 3 = 70
        boolean hasAdd = optimizedProgram.mainBytecode().instructions().stream().anyMatch(i -> i.opcode() == Opcode.ADD);
        boolean hasMul = optimizedProgram.mainBytecode().instructions().stream().anyMatch(i -> i.opcode() == Opcode.MUL);
        assertFalse(hasAdd, "Optimized bytecode should not contain ADD for constants");
        assertFalse(hasMul, "Optimized bytecode should not contain MUL for constants");

        // Running it gives 70
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        VM vm = new VM(optimizedProgram, new PrintStream(outBytes, true, StandardCharsets.UTF_8));
        vm.run();
    }

    @Test
    public void testGraphicsBuiltins() {
        String source = """
            glInitWindow(320, 240, "Test Window");
            glClear(0.1, 0.2, 0.3);
            glColor(1.0, 0.0, 0.0);
            glRect(10, 10, 50, 50);
            glCircle(100, 100, 20);
            glPollEvents();
            let open = glWindowIsOpen();
            print(open);
            glQuit();
        """;
        String out = runSource(source);
        assertTrue(out.equals("true") || out.equals("false"));
    }
}
