package minilang.parser;

import minilang.lexer.Token;

public record Binary(Expr left, Token operator, Expr right) implements Expr {
    @Override
    public String toString() {
        return "(" + left + " " + operator.lexeme() + " " + right + ")";
    }
}
