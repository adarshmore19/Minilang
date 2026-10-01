package minilang.parser;

import minilang.lexer.Token;
import java.util.List;

public record Call(Expr callee, Token paren, List<Expr> arguments) implements Expr {
    @Override
    public String toString() {
        return "(call " + callee + " args=[" + String.join(", ", arguments.stream().map(Expr::toString).toList()) + "])";
    }
}
