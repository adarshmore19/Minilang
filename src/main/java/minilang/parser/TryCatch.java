package minilang.parser;

import minilang.lexer.Token;

public record TryCatch(Stmt tryBlock, Token errorVar, Stmt catchBlock) implements Stmt {
    @Override
    public String toString() {
        return "(try " + tryBlock + " catch " + (errorVar != null ? errorVar.lexeme() : "e") + " " + catchBlock + ")";
    }
}
