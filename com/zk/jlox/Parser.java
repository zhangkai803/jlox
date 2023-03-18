package com.zk.jlox;

import java.util.ArrayList;
import java.util.List;

class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private static class ParseError extends RuntimeException {}

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.VAR)) {
                return varDeclaration();
            }
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        // 变量声明语句
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(TokenType.IF)) {
            // 如果遇到 if 关键字
            return ifStatement();
        }
        if (match(TokenType.PRINT)) {
            // 如果遇到 print 关键字
            return printStatement();
        }
        if (match(TokenType.WHILE)) {
            // 如果遇到 while 关键字
            return whileStatement();
        }
        if (match(TokenType.LEFT_BRACE)) {
            // 如果遇到左大括号 声明代码块
            return new Stmt.Block(block());
        }
        // 其他视为 表达式
        return expressionStatement();
    }

    private Stmt whileStatement() {
        // whileStmt      → "while" "(" expression ")" statement ;
        consume(TokenType.LEFT_PAREN, "Expect '(' after if.");
        Expr expr = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt stmt = statement();
        return new Stmt.While(expr, stmt);
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after if.");
        Expr expr = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(expr, thenBranch, elseBranch);
    }

    private List<Stmt> block() {
        List<Stmt> stmts = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            // 只要不遇到右打括号 或者已经到文件结尾 就持续解析
            stmts.add(declaration());
        }
        // 最后保证代码块以右大括号结尾
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return stmts;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    Expr parseOld() {
        try{
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        // return equality();
        return assignment();
    }

    /*
        第一部分 表达式
        expression     → assignment ;
        assignment     → IDENTIFIER "=" assignment | logic_or ;
        logic_or       → logic_and ("or" logic_and)* ;
        logic_and      → equality ( "and " equality )* ;
        equality       → comparison ( ( "!=" | "==" ) comparison )* ;
        comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
        term           → factor ( ( "-" | "+" ) factor )* ;
        factor         → unary ( ( "/" | "*" ) unary )* ;
        unary          → ( "!" | "-" ) unary | primary ;
        primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER;

        第二部分 语句
        program        → declaration* EOF ;
        declaration    → varDecl | statement ;
        varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
        statement      → exprStmt | ifStmt | printStmt | whileStmt | block ;
        exprStmt       → expression ";" ;
        ifStmt         → "if" "(" expression ")" statement ( "else" statement )? ;
        printStmt      → "print" expression ";" ;
        whileStmt      → "while" "(" expression ")" statement ;
        block          → "{" declaration* "}" ;
     */

    private Expr assignment() {
        // assignment     → IDENTIFIER "=" assignment | logic_or ;
        Expr expr = or();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                // 如果左侧是个变量表达式 认为是赋值操作
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr or() {
        // logic_or       → logic_and ("or" logic_and)* ;
        Expr expr = and();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        // logic_and      → equality ( "and " equality )* ;
        Expr expr = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        // equality       → comparison ( ( "!=" | "==" ) comparison )* ;
        Expr expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        // comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
        Expr expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        // term           → factor ( ( "-" | "+" ) factor )* ;
        Expr expr = factor();
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        // factor         → unary ( ( "/" | "*" ) unary )* ;
        Expr expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        // unary          → ( "!" | "-" ) unary | primary
        while (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        // primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
        if (match(TokenType.TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(TokenType.FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TokenType.NIL)) {
            return new Expr.Literal(null);
        }
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.IDENTIFIER)) {
            // 如果是个标识符 给一个变量声明语句
            return new Expr.Variable(previous());
        }
        if (match(TokenType.LEFT_PAREN)) {
            // 如果遇到左括号 就递归解析括号内部的表达式
            Expr expr = expression();
            // 括号内容解析完成 要检测括号是否闭合
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private Token previous() {
        // 取上一个 token
        return tokens.get(current - 1);
    }

    private boolean match(TokenType... types) {
        // 匹配 token 类型，如果匹配到就前进
        for (TokenType type: types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token advance() {
        // 获取当前 token 并前进
        if(!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean check(TokenType type) {
        // 匹配当前 token 的类型
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private Token peek() {
        // 取当前 token
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        // 判断是否到了最后
        return peek().type == TokenType.EOF;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        // 抛错
        Jlox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) {
                return;
            }
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
