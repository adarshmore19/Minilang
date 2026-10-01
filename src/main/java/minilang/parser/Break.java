package minilang.parser;

import minilang.lexer.Token;

public record Break(Token keyword) implements Stmt {
    @Override
    public String toString() {
        return "(break)";
    }
}
