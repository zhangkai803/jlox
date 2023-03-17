package com.zk.jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fun",    TokenType.FUN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        // 最后添加一个标记结束的 token
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        // 扫描字符并生成 token
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                // 遇到斜杠
                if (match('/')) {
                    // 先检测下一个是不是也是斜杠 是的话当前行就是注释 直接读到换行为止
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    // 不是注释 就直接添加到 token 大概是除法操作
                    addToken(TokenType.SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                // 遇到换行符 行号自增
                line++;
                break;
            case '"': {
                // 如果是双引号 尝试生成 string 字面量
                string();
                break;
            }
            default:
                if (isDigit(c)) {
                    // 如果是数字 尝试生成 number 字面量
                    number();
                } else if (isAlpha(c)) {
                    // 如果是字母 尝试检测标识符
                    identifier();
                } else {
                    Jlox.error(line, "Unexpected character.");
                    break;
                }
        }
    }

    private void identifier() {
        while (isAlphaNumberic(peek())) {
            advance();
        }
        String text = source.substring(start, current);
        // 检测是否命中保留关键字
        TokenType type = keywords.get(text);
        // 如果未命中 那么就是普通标识符
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private boolean isAlphaNumberic(char c) {
        // 是否是字母或数字
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        // 是否是字母
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private void number() {
        // 如果后面还是数字 就继续往前
        while (isDigit(peek())) {
            advance();
        }

        // 如果不是数字 看下一个是不是小数点 并且 小数点后面还有数字
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            // 就继续读小数点后面的数字
            while (isDigit(peek())) {
                advance();
            }
        }

        // 读取完成之后 添加成 token
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peekNext() {
        // 读取下一个字符 但是不向前移动（与 advance 的区别）
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void string() {
        // 匹配字符串字面量
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        if (isAtEnd()) {
            Jlox.error(line, "Unterminated string.");
            return;
        }

        // the closing "
        advance();

        // trim the surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private char peek() {
        // 读取当前字符
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private boolean match(char expected) {
        // 检测下一个字符是否是预期字符 expected
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private void addToken(TokenType type) {
        // 添加一个空 token
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        // 添加一个 token 到列表中
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private char advance() {
        // 读取并向前移动
        return source.charAt(current++);
    }

    private boolean isAtEnd() {
        // 是否到了最后
        return current >= source.length();
    }
}
