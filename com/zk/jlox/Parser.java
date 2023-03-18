package com.zk.jlox;

import java.util.ArrayList;
import java.util.Arrays;
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
        statement      → exprStmt | forStmt | ifStmt | printStmt | whileStmt | block ;
        exprStmt       → expression ";" ;
        forStmt        → "for" "(" ( varDecl | exprStmt | ";" ) expression? ";" expression? ")" statement ;
        ifStmt         → "if" "(" expression ")" statement ( "else" statement )? ;
        printStmt      → "print" expression ";" ;
        whileStmt      → "while" "(" expression ")" statement ;
        block          → "{" declaration* "}" ;
     */

    private Stmt statement() {
        if (match(TokenType.FOR)) {
            return forStatement();
        }
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

    private Stmt forStatement() {
        /**
         * for 语句由几部分组成
         * for ( initializer; condition; increment; ) body;
         * - initializer 初始化器 初始化循环中用到的变量 作用域在循环代码块中 可选
         * - condition 循环条件 满足条件就执行代码块 不满足就会立即跳出 可选
         * - increment 循环结束时的处理 一般用作循环变量自增 用于推进循环进度 可选
         * - body 循环体代码块
         */

        // 首先以左括号开始
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");

        // 初始化器部分
        Stmt initializer;
        if (match(TokenType.SEMICOLON)) {
            // 如果左括号后面直接就是分号 意味着没有循环变量
            initializer = null;
        } else if (match(TokenType.VAR)) {
            // 如果左括号后面是 var 关键字 意味着要声明循环变量
            initializer = varDeclaration();
        } else {
            // 其他情况
            initializer = expressionStatement();
        }

        // 循环条件部分
        Expr condition = null;
        // 循环条件部分也可能没写
        if (!check(TokenType.SEMICOLON)) {
            // 如果当前 token 不是分号 那肯定写了循环条件
            condition = expression();
        }
        // 循环条件最后也需要分号结尾
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

        // 后处理部分
        Expr increment = null;
        if (!check(TokenType.RIGHT_BRACE)) {
            // 只要不是右括号 那么肯定写了后处理部分
            increment = expression();
        }
        // 进入循环代码块之前 要检测以右括号结尾
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");

        // 主代码块部分
        Stmt body = statement();

        // desugaring 脱糖部分 一
        if (increment != null) {
            // 将后处理部分 直接组装到主代码块后面
            Stmt incremeStmt = new Stmt.Expression(increment);
            body = new Stmt.Block(Arrays.asList(body, incremeStmt));
        }
        // 脱糖部分 二
        if (condition == null) {
            // 如果循环条件为空 用字面量 true 替换
            condition = new Expr.Literal(true);
        }
        // 脱糖部分 三
        // for 循环转成 while 循环
        body = new Stmt.While(condition, body);

        // 脱糖部分 四
        if (initializer != null) {
            // 如果有初始化器的话 其只需要执行一次 将语句放在循环体前面再包装一层
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
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
