package minilang.parser;

import minilang.lexer.Token;

public record Continue(Token keyword) implements Stmt {
    @Override
    public String toString() {
        return "(continue)";
    }
}
