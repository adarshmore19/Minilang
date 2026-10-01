package minilang.parser;

import minilang.lexer.Lexer;
import minilang.lexer.Token;
import minilang.lexer.TokenType;
import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent parser for MiniLang 2.0 with OOP support.
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
        if (match(TokenType.IMPORT)) return importStatement();
        if (match(TokenType.CLASS)) return classDeclaration();
        if (match(TokenType.FN)) return functionDeclaration();
        if (match(TokenType.LET)) return varDeclaration();
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.RETURN)) return returnStatement();
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.REPEAT)) return repeatStatement();
        if (match(TokenType.TRY)) return tryStatement();
        if (match(TokenType.BREAK)) return breakStatement();
        if (match(TokenType.CONTINUE)) return continueStatement();
        return expressionStatement();
    }

    private Stmt classDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect class name.");
        consume(TokenType.LBRACE, "Expect '{' before class body.");

        List<FunctionDeclaration> methods = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            if (match(TokenType.FN) || check(TokenType.IDENTIFIER)) {
                methods.add((FunctionDeclaration) functionDeclaration());
            } else {
                throw error(peek(), "Expect method declaration in class body.");
            }
        }
        consume(TokenType.RBRACE, "Expect '}' after class body.");
        return new ClassDeclaration(name, methods);
    }

    private Stmt functionDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect function name.");
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
        if (check(TokenType.SEMICOLON)) {
            match(TokenType.SEMICOLON);
        }
        return new VarDeclaration(name, initializer);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(TokenType.SEMICOLON) && !check(TokenType.RBRACE)) {
            value = assignment();
        }
        if (check(TokenType.SEMICOLON)) {
            match(TokenType.SEMICOLON);
        }
        return new Return(keyword, value);
    }

    private Stmt printStatement() {
        Token printToken = previous();
        Expr arg;
        if (match(TokenType.LPAREN)) {
            if (!check(TokenType.RPAREN)) {
                arg = assignment();
            } else {
                arg = new Literal("");
            }
            consume(TokenType.RPAREN, "Expect ')' after arguments.");
        } else {
            if (!check(TokenType.SEMICOLON) && !check(TokenType.RBRACE) && !isAtEnd()) {
                arg = assignment();
            } else {
                arg = new Literal("");
            }
        }
        if (check(TokenType.SEMICOLON)) match(TokenType.SEMICOLON);
        return new ExpressionStmt(new Call(new Variable(printToken), printToken, List.of(arg)));
    }

    private Stmt importStatement() {
        Token moduleOrPath;
        String path = null;
        if (match(TokenType.STRING)) {
            moduleOrPath = previous();
            path = (String) moduleOrPath.literal();
        } else {
            moduleOrPath = consume(TokenType.IDENTIFIER, "Expect module name or file path after 'import'.");
            path = moduleOrPath.lexeme();
        }
        if (check(TokenType.SEMICOLON)) match(TokenType.SEMICOLON);
        return new ImportStmt(moduleOrPath, path);
    }

    private Stmt repeatStatement() {
        boolean hasParen = match(TokenType.LPAREN);
        Expr count = assignment();
        if (hasParen) consume(TokenType.RPAREN, "Expect ')' after repeat count.");
        if (check(TokenType.IDENTIFIER) && peek().lexeme().equals("times")) {
            advance();
        }
        Stmt body = statement();
        return new Repeat(count, body);
    }

    private Stmt ifStatement() {
        boolean hasParen = match(TokenType.LPAREN);
        Expr condition = assignment();
        if (hasParen) consume(TokenType.RPAREN, "Expect ')' after if condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }
        return new If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        boolean hasParen = match(TokenType.LPAREN);
        Expr condition = assignment();
        if (hasParen) consume(TokenType.RPAREN, "Expect ')' after while condition.");
        Stmt body = statement();
        return new While(condition, body);
    }

    private Stmt forStatement() {
        boolean hasParen = match(TokenType.LPAREN);

        // Case 1: for (x in list) or for x in list
        if (check(TokenType.IDENTIFIER) && peekNext().type() == TokenType.IN) {
            Token var = advance(); // consume variable name
            advance(); // consume 'in'
            Expr collection = assignment();
            if (hasParen) consume(TokenType.RPAREN, "Expect ')' after for-in expression.");
            Stmt body = statement();
            return new ForEach(var, collection, body);
        }

        // Case 2: Traditional C-style for loop: for (init; cond; inc)
        if (!hasParen) {
            throw error(peek(), "Expect '(' or 'variable in collection' after 'for'.");
        }

        Stmt initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.LET)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = assignment();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(TokenType.RPAREN)) {
            increment = assignment();
        }
        consume(TokenType.RPAREN, "Expect ')' after for clauses.");

        Stmt body = statement();
        return new For(initializer, condition, increment, body);
    }

    private Stmt breakStatement() {
        Token keyword = previous();
        if (check(TokenType.SEMICOLON)) match(TokenType.SEMICOLON);
        return new Break(keyword);
    }

    private Stmt continueStatement() {
        Token keyword = previous();
        if (check(TokenType.SEMICOLON)) match(TokenType.SEMICOLON);
        return new Continue(keyword);
    }

    private Stmt expressionStatement() {
        Expr expr = assignment();
        if (check(TokenType.SEMICOLON)) {
            match(TokenType.SEMICOLON);
        }
        return new ExpressionStmt(expr);
    }

    private Stmt tryStatement() {
        Stmt tryBlock = statement();
        consume(TokenType.CATCH, "Expect 'catch' after try block.");
        Token errorVar = null;
        if (match(TokenType.LPAREN)) {
            errorVar = consume(TokenType.IDENTIFIER, "Expect error variable name in catch.");
            consume(TokenType.RPAREN, "Expect ')' after catch parameter.");
        } else if (match(TokenType.IDENTIFIER)) {
            errorVar = previous();
        }
        Stmt catchBlock = statement();
        return new TryCatch(tryBlock, errorVar, catchBlock);
    }

    private Stmt statement() {
        if (match(TokenType.LBRACE)) return new Block(block());
        if (match(TokenType.IMPORT)) return importStatement();
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.REPEAT)) return repeatStatement();
        if (match(TokenType.TRY)) return tryStatement();
        if (match(TokenType.BREAK)) return breakStatement();
        if (match(TokenType.CONTINUE)) return continueStatement();
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
                return new Assignment(variable.name(), value);
            }
            if (expr instanceof IndexGet indexGet) {
                return new IndexSet(indexGet.array(), indexGet.index(), value);
            }
            if (expr instanceof GetExpr getExpr) {
                return new SetExpr(getExpr.object(), getExpr.name(), value);
            }
            throw error(equals, "Invalid assignment target.");
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
            } else if (match(TokenType.EQ)) {
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
        return postfix();
    }

    private Expr postfix() {
        Expr expr = primary();

        while (true) {
            if (match(TokenType.LPAREN)) {
                expr = finishCall(expr);
            } else if (match(TokenType.LBRACKET)) {
                Expr index = assignment();
                consume(TokenType.RBRACKET, "Expect ']' after index.");
                expr = new IndexGet(expr, index);
            } else if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
                expr = new GetExpr(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> args = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do {
                args.add(assignment());
            } while (match(TokenType.COMMA));
        }
        Token paren = consume(TokenType.RPAREN, "Expect ')' after arguments.");
        return new Call(callee, paren, args);
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) return new Literal(false);
        if (match(TokenType.TRUE)) return new Literal(true);
        if (match(TokenType.INTEGER)) return new Literal(previous().literal());
        if (match(TokenType.FLOAT)) return new Literal(previous().literal());
        if (match(TokenType.STRING)) return new Literal(previous().literal());

        if (match(TokenType.THIS, TokenType.SELF)) {
            return new ThisExpr(previous());
        }

        if (match(TokenType.LBRACE)) {
            List<Expr> keys = new ArrayList<>();
            List<Expr> values = new ArrayList<>();
            if (!check(TokenType.RBRACE)) {
                do {
                    Expr key = assignment();
                    consume(TokenType.COLON, "Expect ':' after map key.");
                    Expr val = assignment();
                    keys.add(key);
                    values.add(val);
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RBRACE, "Expect '}' after map entries.");
            return new MapLiteral(keys, values);
        }

        if (match(TokenType.LBRACKET)) {
            List<Expr> elements = new ArrayList<>();
            if (!check(TokenType.RBRACKET)) {
                do {
                    elements.add(assignment());
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RBRACKET, "Expect ']' after array elements.");
            return new ArrayLiteral(elements);
        }

        if (match(TokenType.IDENTIFIER, TokenType.PRINT)) {
            return new Variable(previous());
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

    private Token peekNext() {
        if (current + 1 >= tokens.size()) return tokens.get(tokens.size() - 1);
        return tokens.get(current + 1);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private RuntimeException error(Token token, String message) {
        System.err.println("[Parse Error line " + token.line() + "] " + message + " (found " + token.lexeme() + ")");
        return new RuntimeException(message);
    }
}
