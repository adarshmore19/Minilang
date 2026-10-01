package minilang.parser;

import minilang.lexer.Token;
import java.util.List;

/** Function declaration: fn add(a,b) { ... } */
public record FunctionDeclaration(Token name, List<Token> params, Block body) implements Stmt {
    @Override
    public String toString() {
        return "(fn " + name.lexeme() + " params=[" + String.join(",", params.stream().map(Token::lexeme).toList()) + "] body=" + body + ")";
    }
}
