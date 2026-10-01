package minilang.parser;

public record IndexGet(Expr array, Expr index) implements Expr {
    @Override
    public String toString() {
        return array + "[" + index + "]";
    }
}
