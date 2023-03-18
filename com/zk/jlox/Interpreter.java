package com.zk.jlox;

import java.util.List;

import com.zk.jlox.Expr.Assign;
import com.zk.jlox.Expr.Binary;
import com.zk.jlox.Expr.Grouping;
import com.zk.jlox.Expr.Literal;
import com.zk.jlox.Expr.Unary;
import com.zk.jlox.Expr.Variable;
import com.zk.jlox.Stmt.Block;
import com.zk.jlox.Stmt.Expression;
import com.zk.jlox.Stmt.Print;
import com.zk.jlox.Stmt.Var;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    @Override
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    private void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        // 变量赋值语句
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;  // 这里可以返回 null 赋值语句本身是一个操作 这个操作没有返回值
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        // 变量表达式 即变量访问
        return environment.get(expr.name);
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        // 变量声明语句
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringfy(value));
        return null;
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt stmt: statements) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            Jlox.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

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
