package minilang.parser;

public record IndexSet(Expr array, Expr index, Expr value) implements Expr {
    @Override
    public String toString() {
        return "(= " + array + "[" + index + "] " + value + ")";
    }
}
