package com.zk.jlox;

import com.zk.jlox.Expr.Binary;
import com.zk.jlox.Expr.Grouping;
import com.zk.jlox.Expr.Literal;
import com.zk.jlox.Expr.Unary;

class Interpreter implements Expr.Visitor<Object> {

    void interpret(Expr expr) {
        try {
            Object value = evaluate(expr);
            System.out.println(stringfy(value));
        } catch (RuntimeError error) {
            Jlox.runtimeError(error);
        }
    }

    private String stringfy(Object value) {
        if (value == null) {
            return "nil";
        }
        if (value instanceof Double) {
            String text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return value.toString();
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                // 减法
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case STAR:
                // 乘法
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case SLASH:
                // 除法
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case PLUS:
                // 加法 需要支持字符串连接
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be numbers or strings.");
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
        }
        return null;
    }

    private void checkNumberOperands(Token token, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(token, "Operands must be numbers.");
    }

    private boolean isEqual(Object left, Object right) {
        // 判断两个对象是否相等
        if (left == null && right == null) {
            return true;
        }
        if (left == null) {
            return false;
        }
        return left.equals(right);
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                return -(double)right;
            case BANG:
                return !isTruthy(right);
        }
        return null;
    }

    private Object evaluate(Expr expression) {
        return expression.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

}
