package com.zk.jlox;

import java.util.List;

class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private static class ParseError extends RuntimeException {}

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try{
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
    }

    /*
        expression     → equality ;
        equality       → comparison ( ( "!=" | "==" ) comparison )* ;
        comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
        term           → factor ( ( "-" | "+" ) factor )* ;
        factor         → unary ( ( "/" | "*" ) unary )* ;
        unary          → ( "!" | "-" ) unary | primary ;
        primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
     */

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
        if (match(TokenType.LEFT_PAREN)) {
            // 如果遇到左括号 就递归解析括号内部的表达式
            Expr expr = expression();
            // 括号内容解析完成 要检测括号是否闭合
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        // synchronize(); ?
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
