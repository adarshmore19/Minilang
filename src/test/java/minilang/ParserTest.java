package minilang;

import minilang.lexer.Lexer;
import minilang.parser.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    @Test
    public void testVarDeclaration() {
        String source = "let x = 10 + 20;";
        Parser parser = new Parser(new Lexer(source));
        List<Stmt> statements = parser.parse();

        assertEquals(1, statements.size());
        assertInstanceOf(VarDeclaration.class, statements.get(0));
        VarDeclaration varDecl = (VarDeclaration) statements.get(0);
        assertEquals("x", varDecl.name().lexeme());
        assertInstanceOf(Binary.class, varDecl.initializer());
    }

    @Test
    public void testFunctionDeclaration() {
        String source = "fn add(a, b) { return a + b; }";
        Parser parser = new Parser(new Lexer(source));
        List<Stmt> statements = parser.parse();

        assertEquals(1, statements.size());
        assertInstanceOf(FunctionDeclaration.class, statements.get(0));
        FunctionDeclaration fn = (FunctionDeclaration) statements.get(0);
        assertEquals("add", fn.name().lexeme());
        assertEquals(2, fn.params().size());
        assertEquals("a", fn.params().get(0).lexeme());
        assertEquals("b", fn.params().get(1).lexeme());
    }

    @Test
    public void testIfAndWhileStatements() {
        String source = "if (x > 0) { print(x); } else { print(0); } while (y < 10) { y = y + 1; }";
        Parser parser = new Parser(new Lexer(source));
        List<Stmt> statements = parser.parse();

        assertEquals(2, statements.size());
        assertInstanceOf(If.class, statements.get(0));
        assertInstanceOf(While.class, statements.get(1));
    }
}
