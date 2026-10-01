package minilang.parser;

public record For(Stmt initializer, Expr condition, Expr increment, Stmt body) implements Stmt {
    @Override
    public String toString() {
        return "(for init=" + initializer + " cond=" + condition + " inc=" + increment + " body=" + body + ")";
    }
}
