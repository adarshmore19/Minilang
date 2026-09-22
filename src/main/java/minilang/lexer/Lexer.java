package minilang.lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexer scans MiniLang source code and emits a stream of Tokens.
 *
 * Why a Lexer: lexical analysis is the first step in compilation. It takes raw
 * text and breaks it into meaningful units (tokens) that the parser can
 * interpret. This step also resolves ambiguous characters (whitespace,
 * comments) and validates lexical structure (valid identifiers, numeric
 * literals, string literals, etc.).
 *
 * Operation:
 *   - Scan input character by character
 *   - Group characters into tokens based on current state
 *   - Emit tokens as soon as they're complete
 *   - Handle whitespace and comments (skip them)
 *   - Track line numbers for error reporting
 *
 * Design:
 *   - Using a simple finite state approach (scanning loop with switch)
 *   - State transitions on character type (letter, digit, symbol)
 *   - One-character operators handled directly; multi-character (&&, ||, !=)
 *     peek at next char
 *   - String literals are parsed with escape handling
 */
public class Lexer {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Lexer(String source) {
        this.source = source;
    }

    /**
     * Scan the entire source and return a list of tokens.
     */
    public List<Token> tokenize() {
        while (!isAtEnd()) {
            // skip whitespace and comments
            char c = source.charAt(current);
            if (Character.isWhitespace(c)) {
                handleWhitespace(c);
                continue;
            }
            if (c == '/' && peek() == '*') {
                skipComment();
                continue;
            }
            if (c == '\n') {
                line++;
                current++;
                continue;
            }

            // skip simple whitespace
            if (c == ' ' || c == '\t') {
                current++;
                continue;
            }

            // find the next token
            start = current;
            scanToken();
        }

        // emit EOF token
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    /**
     * Scan one token from the current position.
     */
    private void scanToken() {
        char c = source.charAt(current++);

        switch (c) {
            // single-character punctuation
            case '(' -> addToken(TokenType.LPAREN);
            case ')' -> addToken(TokenType.RPAREN);
            case '{' -> addToken(TokenType.LBRACE);
            case '}' -> addToken(TokenType.RBRACE);
            case ';' -> addToken(TokenType.SEMICOLON);
            case ',' -> addToken(TokenType.COMMA);

            // arithmetic operators
            case '+' -> addToken(TokenType.PLUS);
            case '-' -> addToken(TokenType.MINUS);
            case '*' -> addToken(TokenType.STAR);
            case '/' -> addToken(TokenType.SLASH);
            case '%' -> addToken(TokenType.PERCENT);

            // assignment and comparison operators
            case '=' -> {
                if (peek() == '=') {
                    current++;
                    addToken(TokenType.EQUAL); // == is equality, = is assignment
                } else {
                    addToken(TokenType.EQUAL); // single = for assignment in this lexer model
                }
            }
            case '!' -> {
                if (peek() == '=') {
                    current++;
                    addToken(TokenType.BANG_EQ); // != is inequality
                } else {
                    addToken(TokenType.BANG); // ! is logical not
                }
            }

            // boolean operators
            case '<' -> {
                if (peek() == '=') {
                    current++;
                    addToken(TokenType.LE);
                } else {
                    addToken(TokenType.LT);
                }
            }
            case '>' -> {
                if (peek() == '=') {
                    current++;
                    addToken(TokenType.GE);
                } else {
                    addToken(TokenType.GT);
                }
            }

            case '&' -> {
                if (peek() == '&') {
                    current++;
                    addToken(TokenType.AND);
                } else {
                    error(line, "Invalid character '&'");
                }
            }
            case '|' -> {
                if (peek() == '|') {
                    current++;
                    addToken(TokenType.OR);
                } else {
                    error(line, "Invalid character '|'");
                }
            }

            // string literals
            case '"' -> scanString();

            // identifiers or keywords
            default -> {
                if (Character.isLetter(c)) {
                    scanIdentifier(c);
                } else if (Character.isDigit(c)) {
                    scanNumber(c);
                } else {
                    error(line, "Invalid character: " + c);
                }
            }
        }
    }

    /**
     * Scan an identifier or keyword.
     */
    private void scanIdentifier(char firstChar) {
        while (Character.isLetterOrDigit(peek())) {
            current++;
        }

        String text = source.substring(start, current);
        TokenType type = TokenType.IDENTIFIER;

        // check if it's a keyword
        if (text.equals("let")) type = TokenType.LET;
        else if (text.equals("if")) type = TokenType.IF;
        else if (text.equals("else")) type = TokenType.ELSE;
        else if (text.equals("while")) type = TokenType.WHILE;
        else if (text.equals("fn")) type = TokenType.FN;
        else if (text.equals("return")) type = TokenType.RETURN;
        else if (text.equals("print")) type = TokenType.PRINT;
        else if (text.equals("true")) type = TokenType.TRUE;
        else if (text.equals("false")) type = TokenType.FALSE;

        addToken(type);
    }

    /**
     * Scan a numeric literal.
     */
    private void scanNumber(char firstChar) {
        while (Character.isDigit(peek())) {
            current++;
        }

        String numberText = source.substring(start, current);
        Integer literal = Integer.parseInt(numberText);
        addToken(TokenType.INTEGER, literal);
    }

    /**
     * Scan a string literal (supports escape sequences).
     */
    private void scanString() {
        current++; // skip opening quote
        StringBuilder value = new StringBuilder();

        while (current < source.length() && source.charAt(current) != (char) 34) {
            if (source.charAt(current) == '\\' && current + 1 < source.length()) {
                char next = source.charAt(current + 1);
                switch (next) {
                    case 'n' -> value.append('\n');
                    case 't' -> value.append('\t');
                    case '\\' -> value.append('\\');
                    case '\"' -> value.append('\"');
                    default -> value.append(next);
                }
                current += 2;
                continue;
            }
            value.append(source.charAt(current++));
        }

        if (current >= source.length()) {
            error(line, "Unterminated string literal");
            return;
        }

        current++; // skip closing quote
        addToken(TokenType.STRING, value.toString());
    }

    /**
     * Helper to emit a token with the current lexeme.
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    /**
     * Peek at the next character without consuming it.
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * Check if we've reached the end of the source.
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Handle whitespace (skip spaces and tabs, track newlines).
     */
    private void handleWhitespace(char c) {
        if (c == '\n') {
            line++;
        }
        current++;
    }

    /**
     * Skip C-style block comments.
     */
    private void skipComment() {
        current += 2; // skip '/*'
        while (current < source.length() - 1 && !(
                source.charAt(current) == '*' && source.charAt(current + 1) == '/')) {
            if (source.charAt(current) == '\n') line++;
            current++;
        }
        current += 2; // skip '*/'
    }

    /**
     * Report a lexical error.
     */
    private void error(int line, String message) {
        System.err.println("[Lexical Error line " + line + "] " + message);
        // continue scanning - for now, we'll just report the error
    }
}
