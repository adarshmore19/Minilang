package minilang.parser;

import minilang.lexer.Token;

public record SetExpr(Expr object, Token name, Expr value) implements Expr {
    @Override
    public String toString() {
        return "(set " + object + " ." + name.lexeme() + " = " + value + ")";
    }
}
