package minilang.parser;

import minilang.lexer.Token;

/** Return statement: return x; */
public record Return(Token keyword, Expr value) implements Stmt {
    @Override
    public String toString() {
        return "(return " + value + ")";
    }
}
