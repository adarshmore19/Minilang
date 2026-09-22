package minilang.parser;

import minilang.lexer.Lexer;
import minilang.lexer.Token;
import minilang.lexer.TokenType;
import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent parser for MiniLang.
 *
 * Why recursive descent: it mirrors the grammar directly, making it easy
 * to understand and extend. Each grammar rule becomes a method with the
 * same name, parsing left-to-right with precedence handled by method
 * call nesting.
 *
 * Grammar (adapted for MiniLang):
 *   expression   -> assignment
 *   assignment   -> identifier "=" assignment | logic_or
 *   logic_or     -> logic_and ("||" logic_and)*
 *   logic_and    -> equality ("&&" equality)*
 *   equality     -> comparison (("==" | "!=") comparison)*
 *   comparison   -> term ((">" | ">=" | "<" | "<=") term)*
 *   term         -> factor (("+" | "-") factor)*
 *   factor       -> unary (("/" | "*" | "%") unary)*
 *   unary        -> ("!" | "-") unary | primary
 *   primary      -> INTEGER | STRING | true | false | IDENTIFIER
 *                   | functionCall | "(" expression ")"
 *   statement    -> declaration | expression | block | if | while | return
 *   declaration  -> "let" identifier ("=" assignment)? ";"
 *   block        -> "{" statement* "}"
 *   if           -> "if" "(" expression ")" statement ("else" statement)?
 *   while        -> "while" "(" expression ")" statement
 *   function     -> "fn" identifier "(" params? ")" block
 */
public class Parser {

    private final Lexer lexer;
    private final List<Token> tokens;
    private int current = 0;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.tokens = lexer.tokenize();
    }

    /**
     * Parse the full source into a list of statements.
     */
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    // ── statement parsing ───────────────────────────────────────────

    private Stmt declaration() {
        if (match(TokenType.FN)) return functionDeclaration();
        if (match(TokenType.LET)) return varDeclaration();
        if (match(TokenType.RETURN)) return returnStatement();
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        return expressionStatement();
    }

    private Stmt functionDeclaration() {
        Token name = previous();
        consume(TokenType.IDENTIFIER, "Expect function name.");
        consume(TokenType.LPAREN, "Expect '(' after function name.");
        List<Token> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do {
                params.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RPAREN, "Expect ')' after parameters.");
        consume(TokenType.LBRACE, "Expect '{' before function body.");
        Block body = new Block(block());
        return new FunctionDeclaration(name, params, body);
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = assignment();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new VarDeclaration(name, initializer);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = assignment();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after return value.");
        return new Return(keyword, value);
    }

    private Stmt ifStatement() {
        consume(TokenType.LPAREN, "Expect '(' after 'if'.");
        Expr condition = assignment();
        consume(TokenType.RPAREN, "Expect ')' after if condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }
        return new If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        consume(TokenType.LPAREN, "Expect '(' after 'while'.");
        Expr condition = assignment();
        consume(TokenType.RPAREN, "Expect ')' after while condition.");
        Stmt body = statement();
        return new While(condition, body);
    }

    private Stmt expressionStatement() {
        Expr expr = assignment();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new ExpressionStmt(expr);
    }

    private Stmt statement() {
        if (match(TokenType.LBRACE)) return new Block(block());
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.RETURN)) return returnStatement();
        if (match(TokenType.LET)) return varDeclaration();
        return expressionStatement();
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.RBRACE, "Expect '}' after block.");
        return statements;
    }

    // ── expression parsing ───────────────────────────────────────────

    private Expr assignment() {
        Expr expr = logicOr();
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Variable variable) {
                Token name = variable.name();
                return new Assignment(name, value);
            }
            // Simplified: allow only identifier assignment
            // (full error reporting would check expr type here)
            return new Assignment(((Variable) expr).name(), value);
        }
        return expr;
    }

    private Expr logicOr() {
        Expr expr = logicAnd();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = logicAnd();
            expr = new Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr logicAnd() {
        Expr expr = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (true) {
            if (match(TokenType.BANG_EQ)) {
                Token operator = previous();
                Expr right = comparison();
                expr = new Binary(expr, operator, right);
            } else if (match(TokenType.EQUAL)) {
                Token operator = previous();
                Expr right = comparison();
                expr = new Binary(expr, operator, right);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (true) {
            if (match(TokenType.GT)) {
                Token operator = previous();
                Expr right = term();
                expr = new Binary(expr, operator, right);
            } else if (match(TokenType.GE)) {
                Token operator = previous();
                Expr right = term();
                expr = new Binary(expr, operator, right);
            } else if (match(TokenType.LT)) {
                Token operator = previous();
                Expr right = term();
                expr = new Binary(expr, operator, right);
            } else if (match(TokenType.LE)) {
                Token operator = previous();
                Expr right = term();
                expr = new Binary(expr, operator, right);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (true) {
            if (match(TokenType.PLUS)) {
                Token operator = previous();
                Expr right = factor();
                expr = new Binary(expr, operator, right);
            } else if (match(TokenType.MINUS)) {
                Token operator = previous();
                Expr right = factor();
                expr = new Binary(expr, operator, right);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (true) {
            if (match(TokenType.STAR)) {
                Token operator = previous();
                Expr right = unary();
                expr = new Binary(expr, operator, right);
            } else if (match(TokenType.SLASH)) {
                Token operator = previous();
                Expr right = unary();
                expr = new Binary(expr, operator, right);
            } else if (match(TokenType.PERCENT)) {
                Token operator = previous();
                Expr right = unary();
                expr = new Binary(expr, operator, right);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG)) {
            Token operator = previous();
            Expr right = unary();
            return new Unary(operator, right);
        }
        if (match(TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) return new Literal(false);
        if (match(TokenType.TRUE)) return new Literal(true);
        if (match(TokenType.INTEGER)) return new Literal(previous().literal());
        if (match(TokenType.STRING)) return new Literal(previous().literal());

        if (match(TokenType.IDENTIFIER)) {
            Token name = previous();
            if (match(TokenType.LPAREN)) {
                List<Expr> args = new ArrayList<>();
                if (!check(TokenType.RPAREN)) {
                    do {
                        args.add(assignment());
                    } while (match(TokenType.COMMA));
                }
                consume(TokenType.RPAREN, "Expect ')' after arguments.");
                return new Call(new Variable(name), previous(), args); // simplified
            }
            return new Variable(name);
        }

        if (match(TokenType.LPAREN)) {
            Expr expr = assignment();
            consume(TokenType.RPAREN, "Expect ')' after expression.");
            return expr;
        }

        throw error(peek(), "Expect expression.");
    }

    // ── helpers ─────────────────────────────────────────────────────────

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                current++;
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return previous();
        throw error(peek(), message);
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private RuntimeException error(Token token, String message) {
        System.err.println("[Parse Error line " + token.line() + "] " + message + " (found " + token.lexeme() + ")");
        return new RuntimeException(message);
    }

    // Helper to get identifier from a token
    private Token identifier() {
        return consume(TokenType.IDENTIFIER, "Expect identifier.");
    }
}
