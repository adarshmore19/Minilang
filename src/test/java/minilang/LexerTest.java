package minilang;

import minilang.lexer.Lexer;
import minilang.lexer.Token;
import minilang.lexer.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LexerTest {

    @Test
    public void testTokenizeLiteralsAndPunctuation() {
        String source = "let x = 42; let s = \"hello world\";";
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.LET, tokens.get(0).type());
        assertEquals("x", tokens.get(1).lexeme());
        assertEquals(TokenType.EQUAL, tokens.get(2).type());
        assertEquals(42, tokens.get(3).literal());
        assertEquals(TokenType.SEMICOLON, tokens.get(4).type());

        assertEquals(TokenType.LET, tokens.get(5).type());
        assertEquals("s", tokens.get(6).lexeme());
        assertEquals(TokenType.EQUAL, tokens.get(7).type());
        assertEquals("hello world", tokens.get(8).literal());
        assertEquals(TokenType.SEMICOLON, tokens.get(9).type());
        assertEquals(TokenType.EOF, tokens.get(10).type());
    }

    @Test
    public void testEqualityVsAssignment() {
        String source = "x = 10; if (x == 10) {}";
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.EQUAL, tokens.get(1).type()); // =
        assertEquals(TokenType.EQ, tokens.get(7).type());    // ==
    }

    @Test
    public void testComments() {
        String source = "let a = 1; /* block comment */ let b = 2;";
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        assertEquals("a", tokens.get(1).lexeme());
        assertEquals("b", tokens.get(6).lexeme());
    }

    @Test
    public void testOperators() {
        String source = "+ - * / % != < <= > >= && || !";
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        TokenType[] expected = {
            TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH, TokenType.PERCENT,
            TokenType.BANG_EQ, TokenType.LT, TokenType.LE, TokenType.GT, TokenType.GE,
            TokenType.AND, TokenType.OR, TokenType.BANG, TokenType.EOF
        };

        assertEquals(expected.length, tokens.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], tokens.get(i).type());
        }
    }
}
