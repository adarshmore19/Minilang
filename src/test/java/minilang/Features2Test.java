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

public class Features2Test {

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
    public void testFloatingPointMath() {
        String source = """
            let a = 3.5;
            let b = 2.0;
            print(a + b);
            print(a * b);
            print(7.5 / 2.5);
            print(10 + 0.5);
        """;
        assertEquals("5.5\n7.0\n3.0\n10.5", runSource(source));
    }

    @Test
    public void testDynamicArrays() {
        String source = """
            let arr = [10, 20, 30];
            print(arr[0]);
            arr[1] = 99;
            print(arr);
            print(len(arr));
        """;
        assertEquals("10\n[10, 99, 30]\n3", runSource(source));
    }

    @Test
    public void testArrayPushAndPop() {
        String source = """
            let list = [];
            push(list, 1);
            push(list, 2);
            push(list, "three");
            print(list);
            let popped = pop(list);
            print(popped);
            print(list);
        """;
        assertEquals("[1, 2, three]\nthree\n[1, 2]", runSource(source));
    }

    @Test
    public void testNestedArrays() {
        String source = """
            let matrix = [[1, 2], [3, 4]];
            print(matrix[1][0]);
            matrix[0][1] = 42;
            print(matrix);
        """;
        assertEquals("3\n[[1, 42], [3, 4]]", runSource(source));
    }

    @Test
    public void testForLoop() {
        String source = """
            let total = 0;
            for (let i = 1; i <= 4; i = i + 1) {
                total = total + i;
            }
            print(total);
        """;
        assertEquals("10", runSource(source));
    }

    @Test
    public void testBreakInLoop() {
        String source = """
            let i = 0;
            while (i < 10) {
                if (i == 3) {
                    break;
                }
                i = i + 1;
            }
            print(i);
        """;
        assertEquals("3", runSource(source));
    }

    @Test
    public void testContinueInForLoop() {
        String source = """
            let sum = 0;
            for (let i = 1; i <= 5; i = i + 1) {
                if (i == 3) {
                    continue;
                }
                sum = sum + i;
            }
            print(sum);
        """;
        assertEquals("12", runSource(source));
    }

    @Test
    public void testMathBuiltins() {
        String source = """
            print(sqrt(144));
            print(abs(-42));
            print(min(10, 25));
            print(max(10, 25));
            print(floor(4.9));
            print(ceil(4.1));
            print(round(4.6));
            print(pow(2, 3));
        """;
        assertEquals("12.0\n42\n10\n25\n4\n5\n5\n8.0", runSource(source));
    }

    @Test
    public void testTypeAndConversionBuiltins() {
        String source = """
            print(type(42));
            print(type(3.14));
            print(type("abc"));
            print(type([1, 2]));
            print(int("123") + 1);
            print(float("2.5") * 2);
            print(str(100) + " items");
        """;
        assertEquals("int\nfloat\nstring\narray\n124\n5.0\n100 items", runSource(source));
    }

    @Test
    public void testStringIndexingAndLength() {
        String source = """
            let msg = "Hello";
            print(len(msg));
            print(msg[0]);
            print(msg[4]);
        """;
        assertEquals("5\nH\no", runSource(source));
    }

    @Test
    public void testSingleLineComments() {
        String source = """
            // This is a comment
            let x = 100; // Inline comment
            // Another comment
            print(x);
        """;
        assertEquals("100", runSource(source));
    }
}
