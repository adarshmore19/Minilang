package minilang;

import minilang.compiler.Compiler;
import minilang.compiler.Program;
import minilang.lexer.Lexer;
import minilang.lexer.Token;
import minilang.parser.Parser;
import minilang.parser.Stmt;
import minilang.vm.ArrayValue;
import minilang.vm.IntValue;
import minilang.vm.StringValue;
import minilang.vm.VM;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KidFriendlyAndLibrariesTest {

    private String run(String source) {
        List<Stmt> statements = new Parser(new Lexer(source)).parse();
        Compiler compiler = new Compiler();
        Program program = compiler.compile(statements);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        VM vm = new VM(program, new PrintStream(out));
        vm.run();
        return out.toString().trim();
    }

    @Test
    public void testKidFriendlySyntaxAndAutoAssign() {
        String code = """
            name = "Alex"
            score = 100
            score = score + 50
            say name
            say score
            """;
        assertEquals("Alex\n150", run(code));
    }

    @Test
    public void testEnglishOperatorsAndParenthesisFreeIf() {
        String code = """
            score = 85
            isHappy = true
            
            if score >= 80 and isHappy {
                say "Great job!"
            }
            
            if score is 100 or not (score < 50) {
                say "Passed!"
            }
            """;
        assertEquals("Great job!\nPassed!", run(code));
    }

    @Test
    public void testStringRepetition() {
        String code = """
            cheer = "Go! " * 3
            say cheer
            chorus = 2 * "Na " + "Batman"
            say chorus
            """;
        assertEquals("Go! Go! Go! \nNa Na Batman", run(code));
    }

    @Test
    public void testRepeatLoop() {
        String code = """
            total = 0
            repeat 4 times {
                total = total + 5
            }
            say total
            
            count = 0
            repeat 3 {
                count = count + 1
            }
            say count
            """;
        assertEquals("20\n3", run(code));
    }

    @Test
    public void testForInRange() {
        String code = """
            sum = 0
            for i in range(5) {
                sum = sum + i
            }
            say sum
            
            total = 0
            for x in range(1, 6) {
                total = total + x
            }
            say total
            """;
        assertEquals("10\n15", run(code));
    }

    @Test
    public void testForEachArrayAndString() {
        String code = """
            items = ["apple", "banana", "cherry"]
            for fruit in items {
                say fruit
            }
            
            msg = ""
            for ch in "code" {
                msg = msg + ch + "-"
            }
            say msg
            """;
        assertEquals("apple\nbanana\ncherry\nc-o-d-e-", run(code));
    }

    @Test
    public void testAlgorithmLibrary() {
        String code = """
            numbers = [5, 2, 9, 1, 5, 6]
            say sum(numbers)
            say mean([10, 20, 30])
            say median([1, 5, 2])
            say median([1, 2, 3, 4])
            
            sorted = sort(numbers)
            say sorted
            
            reversedNums = reverse([1, 2, 3])
            say reversedNums
            
            say reverse("desserts")
            
            say binarySearch([10, 20, 30, 40, 50], 30)
            say binarySearch([10, 20, 30, 40, 50], 99)
            """;
        String out = run(code);
        String[] lines = out.split("\n");
        assertEquals("28", lines[0]);
        assertEquals("20.0", lines[1]);
        assertEquals("2", lines[2]);
        assertEquals("2.5", lines[3]);
        assertEquals("[1, 2, 5, 5, 6, 9]", lines[4]);
        assertEquals("[3, 2, 1]", lines[5]);
        assertEquals("stressed", lines[6]);
        assertEquals("2", lines[7]);
        assertEquals("-1", lines[8]);
    }

    @Test
    public void testPlotEngineSave() {
        String code = """
            import plot
            plotClear()
            plotTitle("Test Math Curves")
            plotXLabel("X Values")
            plotYLabel("Y Values")
            plotLine([0, 1, 2, 3, 4], [0, 1, 4, 9, 16], "Parabola")
            plotScatter([0, 1, 2, 3, 4], [0, 2, 3, 5, 7], "Points")
            saved = plotSave("target/test_plot.png", 600, 400)
            say saved
            """;
        assertEquals("true", run(code));
        assertTrue(new File("target/test_plot.png").exists());
    }

    @Test
    public void testTurtleEngineSave() {
        String code = """
            import turtle
            turtleInit(400, 400, "Test Turtle")
            penSize(3)
            penColor(255, 0, 0)
            repeat 4 {
                forward(50)
                turnRight(90)
            }
            saved = turtleSave("target/test_turtle.png")
            say saved
            """;
        assertEquals("true", run(code));
        assertTrue(new File("target/test_turtle.png").exists());
    }

    @Test
    public void testImportFile() throws Exception {
        File helperFile = new File("target/test_helper.ml");
        Files.writeString(helperFile.toPath(), """
            def doubleVal(x) {
                return x * 2
            }
            """);

        String code = """
            import "target/test_helper.ml"
            say doubleVal(21)
            """;
        assertEquals("42", run(code));
    }
}
