package minilang.parser;

import minilang.lexer.Token;

public record ThisExpr(Token keyword) implements Expr {
    @Override
    public String toString() {
        return keyword.lexeme();
    }
}
