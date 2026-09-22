package minilang.lexer;

/**
 * TokenType enumerates every kind of token the MiniLang lexer can emit.
 *
 * Why an enum: tokens are a fixed, small set of categories. An enum gives
 * exhaustiveness checks in the parser and compiler, and makes the token
 * stream easy to inspect in debug output.
 *
 * Categories:
 *  - single-character punctuation: LPAREN, RPAREN, LBRACE, RBRACE, SEMICOLON, COMMA
 *  - multi-character operators: PLUS, MINUS, STAR, SLASH, PERCENT
 *    EQ, BANG_EQ, LT, LE, GT, GE, AND, OR
 *  - literals: INTEGER, STRING, TRUE, FALSE
 *  - identifiers and keywords: IDENTIFIER, LET, IF, ELSE, WHILE, FN, RETURN, PRINT
 *  - EOF marks end of input
 */
public enum TokenType {

    // ── single-character punctuation ──────────────────────────────────────
    LPAREN,        // (
    RPAREN,        // )
    LBRACE,        // {
    RBRACE,        // }
    SEMICOLON,     // ;
    COMMA,         // ,

    // ── operators ────────────────────────────────────────────────────────
    PLUS,          // +
    MINUS,         // -
    STAR,          // *
    SLASH,         // /
    PERCENT,       // %

    EQUAL,         // =
    BANG,          // !
    BANG_EQ,       // !=
    LT,            // <
    LE,            // <=
    GT,            // >
    GE,            // >=
    AND,           // &&
    OR,            // ||

    // ── literals ─────────────────────────────────────────────────────────
    INTEGER,       // 42
    STRING,        // "hello"

    // ── keywords ─────────────────────────────────────────────────────────
    LET,           // let
    IF,            // if
    ELSE,          // else
    WHILE,         // while
    FN,            // fn
    RETURN,        // return
    PRINT,         // print
    TRUE,          // true
    FALSE,         // false

    // ── identifier ───────────────────────────────────────────────────────
    IDENTIFIER,    // variable / function names

    // ── end of input ─────────────────────────────────────────────────────
    EOF
}