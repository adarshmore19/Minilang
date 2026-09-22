package minilang.parser;

import minilang.lexer.Token;

/**
 * Statement nodes for the MiniLang AST.
 */
interface Stmt {}

/** Variable declaration: let x = 10; */
record VarDeclaration(Token name, Expr initializer) implements Stmt {
    @Override
    public String toString() {
        return "(var " + name.lexeme() + " = " + initializer + ")";
    }
}

/** Expression statement: x + 5; */
record ExpressionStmt(Expr expression) implements Stmt {
    @Override
    public String toString() {
        return "(expr " + expression + ")";
    }
}

/** Block of statements: { ... } */
record Block(java.util.List<Stmt> statements) implements Stmt {
    @Override
    public String toString() {
        return "(block " + String.join(" ", statements.stream().map(Stmt::toString).toList()) + ")";
    }
}

/** If statement: if (cond) { ... } else { ... } */
record If(Expr condition, Stmt thenBranch, Stmt elseBranch) implements Stmt {
    @Override
    public String toString() {
        String elseStr = elseBranch != null ? " else=" + elseBranch : "";
        return "(if " + condition + " then=" + thenBranch + elseStr + ")";
    }
}

/** While loop: while (cond) { ... } */
record While(Expr condition, Stmt body) implements Stmt {
    @Override
    public String toString() {
        return "(while " + condition + " body=" + body + ")";
    }
}

/** Function declaration: fn add(a,b) { ... } */
record FunctionDeclaration(Token name, java.util.List<Token> params, Block body) implements Stmt {
    @Override
    public String toString() {
        return "(fn " + name.lexeme() + " params=[" + String.join(",", params.stream().map(Token::lexeme).toList()) + "] body=" + body + ")";
    }
}

/** Return statement: return x; */
record Return(Token keyword, Expr value) implements Stmt {
    @Override
    public String toString() {
        return "(return " + value + ")";
    }
}
