package minilang.parser;

import java.util.List;

public record ArrayLiteral(List<Expr> elements) implements Expr {
    @Override
    public String toString() {
        return "[" + String.join(", ", elements.stream().map(Expr::toString).toList()) + "]";
    }
}
