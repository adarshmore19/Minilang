package minilang.parser;

import minilang.lexer.Token;

public record Variable(Token name) implements Expr {
    @Override
    public String toString() {
        return name.lexeme();
    }
}
