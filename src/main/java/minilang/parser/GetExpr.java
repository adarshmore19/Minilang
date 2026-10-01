package minilang.parser;

import minilang.lexer.Token;

public record GetExpr(Expr object, Token name) implements Expr {
    @Override
    public String toString() {
        return "(get " + object + " ." + name.lexeme() + ")";
    }
}
