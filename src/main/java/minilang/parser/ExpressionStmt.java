package minilang.parser;

/** Expression statement: x + 5; */
public record ExpressionStmt(Expr expression) implements Stmt {
    @Override
    public String toString() {
        return "(expr " + expression + ")";
    }
}
