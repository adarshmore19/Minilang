package minilang.lexer;

/**
 * TokenType enumerates every kind of token the MiniLang lexer can emit.
 */
public enum TokenType {

    // ── punctuation ───────────────────────────────────────────────────────
    LPAREN,        // (
    RPAREN,        // )
    LBRACE,        // {
    RBRACE,        // }
    LBRACKET,      // [
    RBRACKET,      // ]
    SEMICOLON,     // ;
    COLON,         // :
    COMMA,         // ,
    DOT,           // .

    // ── operators ────────────────────────────────────────────────────────
    PLUS,          // +
    MINUS,         // -
    STAR,          // *
    SLASH,         // /
    PERCENT,       // %

    EQUAL,         // =
    EQ,            // ==
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
    FLOAT,         // 3.14
    STRING,        // "hello"

    // ── keywords ─────────────────────────────────────────────────────────
    LET,           // let
    IF,            // if
    ELSE,          // else
    WHILE,         // while
    FOR,           // for
    BREAK,         // break
    CONTINUE,      // continue
    CLASS,         // class
    THIS,          // this
    SELF,          // self
    FN,            // fn
    RETURN,        // return
    PRINT,         // print, say, show
    TRUE,          // true
    FALSE,         // false
    IMPORT,        // import
    REPEAT,        // repeat, loop
    IN,            // in
    TRY,           // try
    CATCH,         // catch

    // ── identifier ───────────────────────────────────────────────────────
    IDENTIFIER,    // variable / function names

    // ── end of input ─────────────────────────────────────────────────────
    EOF
}