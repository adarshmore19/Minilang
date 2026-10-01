package minilang.parser;

import minilang.lexer.Token;

/** Variable declaration: let x = 10; */
public record VarDeclaration(Token name, Expr initializer) implements Stmt {
    @Override
    public String toString() {
        return "(var " + name.lexeme() + " = " + initializer + ")";
    }
}
