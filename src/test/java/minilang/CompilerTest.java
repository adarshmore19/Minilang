package minilang;

import minilang.compiler.Compiler;
import minilang.compiler.Opcode;
import minilang.compiler.Program;
import minilang.lexer.Lexer;
import minilang.parser.Parser;
import minilang.parser.Stmt;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CompilerTest {

    @Test
    public void testCompileArithmetic() {
        String source = "let x = 5 + 3 * 2;";
        List<Stmt> statements = new Parser(new Lexer(source)).parse();
        Compiler compiler = new Compiler();
        Program program = compiler.compile(statements);

        assertNotNull(program.mainBytecode());
        assertTrue(program.mainBytecode().instructions().stream().anyMatch(i -> i.opcode() == Opcode.ADD));
        assertTrue(program.mainBytecode().instructions().stream().anyMatch(i -> i.opcode() == Opcode.MUL));
        assertTrue(program.mainBytecode().instructions().stream().anyMatch(i -> i.opcode() == Opcode.STORE_LOCAL));
        assertTrue(program.mainBytecode().instructions().stream().anyMatch(i -> i.opcode() == Opcode.HALT));
    }

    @Test
    public void testCompileFunction() {
        String source = "fn double(x) { return x * 2; } let res = double(4);";
        List<Stmt> statements = new Parser(new Lexer(source)).parse();
        Compiler compiler = new Compiler();
        Program program = compiler.compile(statements);

        assertEquals(1, program.functions().size());
        assertTrue(program.functions().containsKey(0));
        assertEquals("double", program.functions().get(0).name());
        assertEquals(1, program.functions().get(0).paramCount());
    }
}
