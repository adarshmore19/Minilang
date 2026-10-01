package minilang.parser;

import minilang.lexer.Token;

public record Assignment(Token name, Expr value) implements Expr {
    @Override
    public String toString() {
        return "(= " + name.lexeme() + " " + value + ")";
    }
}
