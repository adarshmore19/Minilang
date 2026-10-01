package minilang.parser;

public record Literal(Object value) implements Expr {
    @Override
    public String toString() {
        if (value instanceof String s) {
            return "\"" + s + "\"";
        }
        return String.valueOf(value);
    }
}
