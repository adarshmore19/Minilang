package minilang.parser;

public record Repeat(Expr count, Stmt body) implements Stmt {
    @Override
    public String toString() {
        return "repeat (" + count + ") " + body;
    }
}
