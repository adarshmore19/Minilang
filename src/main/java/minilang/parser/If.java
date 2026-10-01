package minilang.parser;

/** If statement: if (cond) { ... } else { ... } */
public record If(Expr condition, Stmt thenBranch, Stmt elseBranch) implements Stmt {
    @Override
    public String toString() {
        String elseStr = elseBranch != null ? " else=" + elseBranch : "";
        return "(if " + condition + " then=" + thenBranch + elseStr + ")";
    }
}
