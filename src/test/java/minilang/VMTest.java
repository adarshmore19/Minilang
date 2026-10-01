package minilang;

import minilang.compiler.Compiler;
import minilang.compiler.Program;
import minilang.lexer.Lexer;
import minilang.parser.Parser;
import minilang.parser.Stmt;
import minilang.vm.VM;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VMTest {

    private String runSource(String source) {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outBytes, true, StandardCharsets.UTF_8);

        List<Stmt> statements = new Parser(new Lexer(source)).parse();
        Program program = new Compiler().compile(statements);
        VM vm = new VM(program, printStream);
        vm.run();

        return outBytes.toString(StandardCharsets.UTF_8).trim();
    }

    @Test
    public void testArithmetic() {
        assertEquals("42", runSource("print(10 + 2 * 16);"));
        assertEquals("7", runSource("print(20 / 3 + 1 % 2);"));
    }

    @Test
    public void testVariablesAndReassignment() {
        String source = """
            let a = 10;
            let b = 20;
            a = a + 5;
            print(a + b);
        """;
        assertEquals("35", runSource(source));
    }

    @Test
    public void testComparisonsAndBooleans() {
        String source = """
            let x = 10;
            if (x > 5 && x <= 10) {
                print("yes");
            } else {
                print("no");
            }
        """;
        assertEquals("yes", runSource(source));
    }

    @Test
    public void testWhileLoop() {
        String source = """
            let sum = 0;
            let i = 1;
            while (i <= 5) {
                sum = sum + i;
                i = i + 1;
            }
            print(sum);
        """;
        assertEquals("15", runSource(source));
    }

    @Test
    public void testFunctionsAndRecursion() {
        String source = """
            fn factorial(n) {
                if (n <= 1) {
                    return 1;
                }
                return n * factorial(n - 1);
            }
            print(factorial(5));
        """;
        assertEquals("120", runSource(source));
    }

    @Test
    public void testFinalAcceptance() {
        String source = """
            fn factorial(n) {
                if (n <= 1) {
                    return 1;
                }
                return n * factorial(n - 1);
            }
            let result = factorial(5);
            if (result == 120) {
                print("MiniLang works!");
            } else {
                print("Something is wrong.");
            }
        """;
        assertEquals("MiniLang works!", runSource(source));
    }
}
