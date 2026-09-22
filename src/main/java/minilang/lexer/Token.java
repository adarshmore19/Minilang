package minilang.lexer;

/**
 * Token represents a single lexical token produced by the Lexer.
 *
 * Why this shape: a token needs three things to be useful downstream:
 *   - type:   what category of token it is (TokenType enum)
 *   - lexeme: the exact source text that produced it (useful for error
 *             messages and for identifiers/keywords)
 *   - literal: the runtime value for literal tokens (an Integer for
 *             INTEGER, a String for STRING). Null for everything else.
 *   - line:   source line number, so errors can point at the user's code
 *
 * Using a record keeps this data structure small and immutable; the
 * compiler and VM never mutate tokens, they only read them.
 */
public record Token(
    TokenType type,
    String lexeme,
    Object literal,
    int line
) {
    /**
     * Convenience constructor for tokens without a literal value
     * (operators, punctuation, identifiers, keywords).
     */
    public Token(TokenType type, String lexeme, int line) {
        this(type, lexeme, null, line);
    }

    @Override
    public String toString() {
        if (literal != null) {
            return type + " " + lexeme + " (" + literal + ")";
        }
        return type + " " + lexeme;
    }
}