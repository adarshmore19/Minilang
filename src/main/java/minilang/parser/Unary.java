package minilang.parser;

import minilang.lexer.Token;

public record Unary(Token operator, Expr right) implements Expr {
    @Override
    public String toString() {
        return "(" + operator.lexeme() + right + ")";
    }
}
