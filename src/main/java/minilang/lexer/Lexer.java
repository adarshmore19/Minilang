package minilang.lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexer scans MiniLang source code and emits a stream of Tokens.
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
            char c = source.charAt(current);
            if (Character.isWhitespace(c)) {
                handleWhitespace(c);
                continue;
            }
            if (c == '/' && peekNext() == '*') {
                skipBlockComment();
                continue;
            }
            if (c == '/' && peekNext() == '/') {
                skipLineComment();
                continue;
            }
            if (c == '\n') {
                line++;
                current++;
                continue;
            }
            if (c == ' ' || c == '\t' || c == '\r') {
                current++;
                continue;
            }

            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void scanToken() {
        char c = source.charAt(current++);

        switch (c) {
            case '(' -> addToken(TokenType.LPAREN);
            case ')' -> addToken(TokenType.RPAREN);
            case '{' -> addToken(TokenType.LBRACE);
            case '}' -> addToken(TokenType.RBRACE);
            case '[' -> addToken(TokenType.LBRACKET);
            case ']' -> addToken(TokenType.RBRACKET);
            case ';' -> addToken(TokenType.SEMICOLON);
            case ':' -> addToken(TokenType.COLON);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);

            case '+' -> addToken(TokenType.PLUS);
            case '-' -> addToken(TokenType.MINUS);
            case '*' -> addToken(TokenType.STAR);
            case '/' -> addToken(TokenType.SLASH);
            case '%' -> addToken(TokenType.PERCENT);

            case '=' -> {
                if (peek() == '=') {
                    current++;
                    addToken(TokenType.EQ);
                } else {
                    addToken(TokenType.EQUAL);
                }
            }
            case '!' -> {
                if (peek() == '=') {
                    current++;
                    addToken(TokenType.BANG_EQ);
                } else {
                    addToken(TokenType.BANG);
                }
            }

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

            case '"' -> scanString();

            default -> {
                if (Character.isLetter(c) || c == '_') {
                    scanIdentifier(c);
                } else if (Character.isDigit(c)) {
                    scanNumber(c);
                } else {
                    error(line, "Invalid character: " + c);
                }
            }
        }
    }

    private void scanIdentifier(char firstChar) {
        while (Character.isLetterOrDigit(peek()) || peek() == '_') {
            current++;
        }

        String text = source.substring(start, current);
        TokenType type = TokenType.IDENTIFIER;

        if (text.equals("let")) type = TokenType.LET;
        else if (text.equals("if")) type = TokenType.IF;
        else if (text.equals("else")) type = TokenType.ELSE;
        else if (text.equals("while")) type = TokenType.WHILE;
        else if (text.equals("for")) type = TokenType.FOR;
        else if (text.equals("break")) type = TokenType.BREAK;
        else if (text.equals("continue")) type = TokenType.CONTINUE;
        else if (text.equals("class")) type = TokenType.CLASS;
        else if (text.equals("this")) type = TokenType.THIS;
        else if (text.equals("self")) type = TokenType.SELF;
        else if (text.equals("fn") || text.equals("def") || text.equals("function")) type = TokenType.FN;
        else if (text.equals("return")) type = TokenType.RETURN;
        else if (text.equals("print") || text.equals("say") || text.equals("show")) type = TokenType.PRINT;
        else if (text.equals("true")) type = TokenType.TRUE;
        else if (text.equals("false")) type = TokenType.FALSE;
        else if (text.equals("import")) type = TokenType.IMPORT;
        else if (text.equals("repeat") || text.equals("loop")) type = TokenType.REPEAT;
        else if (text.equals("in")) type = TokenType.IN;
        else if (text.equals("and")) type = TokenType.AND;
        else if (text.equals("or")) type = TokenType.OR;
        else if (text.equals("not")) type = TokenType.BANG;
        else if (text.equals("is")) type = TokenType.EQ;
        else if (text.equals("try")) type = TokenType.TRY;
        else if (text.equals("catch")) type = TokenType.CATCH;

        addToken(type);
    }

    private void scanNumber(char firstChar) {
        while (Character.isDigit(peek())) {
            current++;
        }

        if (peek() == '.' && Character.isDigit(peekNext())) {
            current++; // consume '.'
            while (Character.isDigit(peek())) {
                current++;
            }
            String numberText = source.substring(start, current);
            Double literal = Double.parseDouble(numberText);
            addToken(TokenType.FLOAT, literal);
            return;
        }

        String numberText = source.substring(start, current);
        Integer literal = Integer.parseInt(numberText);
        addToken(TokenType.INTEGER, literal);
    }

    private void scanString() {
        if (peek() == '"' && peekNext() == '"') {
            current += 2; // consume 2nd and 3rd quote
            scanMultilineString();
            return;
        }

        StringBuilder value = new StringBuilder();

        while (current < source.length() && source.charAt(current) != '"') {
            if (source.charAt(current) == '\n') line++;
            if (source.charAt(current) == '\\' && current + 1 < source.length()) {
                char next = source.charAt(current + 1);
                switch (next) {
                    case 'n' -> value.append('\n');
                    case 't' -> value.append('\t');
                    case 'r' -> value.append('\r');
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

    private void scanMultilineString() {
        StringBuilder value = new StringBuilder();
        while (current < source.length()) {
            if (source.charAt(current) == '"' && current + 2 < source.length()
                    && source.charAt(current + 1) == '"' && source.charAt(current + 2) == '"') {
                current += 3; // consume closing triple quotes
                addToken(TokenType.STRING, value.toString());
                return;
            }
            if (source.charAt(current) == '\n') {
                line++;
            }
            value.append(source.charAt(current++));
        }
        error(line, "Unterminated multi-line string literal");
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void handleWhitespace(char c) {
        if (c == '\n') {
            line++;
        }
        current++;
    }

    private void skipBlockComment() {
        current += 2; // skip '/*'
        while (current < source.length() - 1 && !(
                source.charAt(current) == '*' && source.charAt(current + 1) == '/')) {
            if (source.charAt(current) == '\n') line++;
            current++;
        }
        if (current < source.length() - 1) {
            current += 2; // skip '*/'
        } else {
            current = source.length();
            error(line, "Unterminated block comment");
        }
    }

    private void skipLineComment() {
        current += 2; // skip '//'
        while (!isAtEnd() && source.charAt(current) != '\n') {
            current++;
        }
    }

    private void error(int line, String message) {
        System.err.println("[Lexical Error line " + line + "] " + message);
    }
}
