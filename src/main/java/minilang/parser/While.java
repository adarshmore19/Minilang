package minilang.parser;

/** While loop: while (cond) { ... } */
public record While(Expr condition, Stmt body) implements Stmt {
    @Override
    public String toString() {
        return "(while " + condition + " body=" + body + ")";
    }
}
