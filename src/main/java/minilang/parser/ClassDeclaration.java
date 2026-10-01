package minilang.parser;

import minilang.lexer.Token;
import java.util.List;

public record ClassDeclaration(Token name, List<FunctionDeclaration> methods) implements Stmt {
    @Override
    public String toString() {
        return "(class " + name.lexeme() + " methods=" + methods + ")";
    }
}
