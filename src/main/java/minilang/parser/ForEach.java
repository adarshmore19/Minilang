package minilang.parser;

import minilang.lexer.Token;

public record ForEach(Token variable, Expr collection, Stmt body) implements Stmt {
    @Override
    public String toString() {
        return "for " + variable.lexeme() + " in " + collection + " " + body;
    }
}
