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

public class AdvancedFeaturesTest {

    private String run(String source) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Stmt> statements = new Parser(new Lexer(source)).parse();
        Program program = new Compiler(true).compile(statements);
        VM vm = new VM(program, new PrintStream(out, true, StandardCharsets.UTF_8));
        vm.run();
        return out.toString(StandardCharsets.UTF_8).trim();
    }

    @Test
    public void testMapCreationAndAccess() {
        String code = """
            user = {"name": "Charlie", "level": 5, "active": true}
            say user["name"]
            say user["level"]
            say user["active"]
            say len(user)
            """;
        assertEquals("Charlie\n5\ntrue\n3", run(code));
    }

    @Test
    public void testMapMutationAndBuiltins() {
        String code = """
            player = {"score": 100}
            player["score"] = 250
            player["rank"] = "Gold"
            say player["score"]
            say player["rank"]
            say has(player, "score")
            say has(player, "missing")
            say keys(player)
            say values(player)
            """;
        assertEquals("250\nGold\ntrue\nfalse\n[score, rank]\n[250, Gold]", run(code));
    }

    @Test
    public void testMultilineStrings() {
        String code = """
            art = \"\"\"
            * * *
            HELLO
            * * *
            \"\"\".trim()
            say art
            """;
        // Simple multiline string test without .trim()
        String code2 = """
            msg = \"\"\"Line 1
            Line 2
            Line 3\"\"\"
            say msg
            """;
        assertEquals("Line 1\nLine 2\nLine 3", run(code2));
    }

    @Test
    public void testFileIO() {
        String code = """
            path = "target/mini_test_file.txt"
            writeFile(path, "First line of text.")
            say fileExists(path)
            say readFile(path)
            appendFile(path, " Appended text.")
            say readFile(path)
            deleted = deleteFile(path)
            say deleted
            say fileExists(path)
            """;
        String expected = "true\nFirst line of text.\nFirst line of text. Appended text.\ntrue\nfalse";
        assertEquals(expected, run(code));
    }

    @Test
    public void testTryCatchHandling() {
        String code = """
            status = "initial"
            try {
                badMath = 100 / 0
                status = "unreachable"
            } catch (err) {
                status = "recovered from: " + err
            }
            say status
            """;
        assertEquals("recovered from: Division by zero.", run(code));
    }

    @Test
    public void testTryCatchNoError() {
        String code = """
            result = 0
            try {
                result = 40 + 2
            } catch (err) {
                result = -1
            }
            say result
            """;
        assertEquals("42", run(code));
    }
}
