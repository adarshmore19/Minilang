package minilang.parser;

import minilang.lexer.Token;

public record ImportStmt(Token moduleOrPath, String path) implements Stmt {
    @Override
    public String toString() {
        return "import " + (path != null ? "\"" + path + "\"" : moduleOrPath.lexeme());
    }
}
