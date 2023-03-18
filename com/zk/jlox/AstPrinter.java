package com.zk.jlox;

import com.zk.jlox.Expr.Assign;
import com.zk.jlox.Expr.Call;
import com.zk.jlox.Expr.Logical;
import com.zk.jlox.Expr.Variable;

class AstPrinter implements Expr.Visitor<String> {

    @Override
    public String visitCallExpr(Call expr) {
        return null;
    }

    @Override
    public String visitLogicalExpr(Logical expr) {
        return null;
    }

    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitAssignExpr(Assign expr) {
        return expr.accept(this);
    }

    @Override
    public String visitVariableExpr(Variable expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
