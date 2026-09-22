package minilang.parser;
import minilang.lexer.Token;
public interface Expr {
    String toString();
}
record Binary(Expr left, Token operator, Expr right) implements Expr {
    public String toString() { return "(" + left + " " + operator.lexeme() + " " + right + ")"; }
}
record Unary(Token operator, Expr right) implements Expr {
    public String toString() { return "(" + operator.lexeme() + right + ")"; }
}
record Variable(Token name) implements Expr {
    public String toString() { return name.lexeme(); }
}
record Literal(Object value) implements Expr {
    public String toString() { if (value instanceof String s) return "\"" + s + "\""; return String.valueOf(value); }
}
record Assignment(Token name, Expr value) implements Expr {
    public String toString() { return "(= " + name.lexeme() + " " + value + ")"; }
}
record Call(Expr callee, Token paren, java.util.List<Expr> arguments) implements Expr {
    public String toString() { return "(call " + callee + " args=[" + String.join(", ", arguments.stream().map(Expr::toString).toList()) + "])"; }
}
