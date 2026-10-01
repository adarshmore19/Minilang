package minilang.parser;

import java.util.List;

/** Block of statements: { ... } */
public record Block(List<Stmt> statements) implements Stmt {
    @Override
    public String toString() {
        return "(block " + String.join(" ", statements.stream().map(Stmt::toString).toList()) + ")";
    }
}
