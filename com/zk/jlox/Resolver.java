package com.zk.jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.zk.jlox.Expr.Assign;
import com.zk.jlox.Expr.Binary;
import com.zk.jlox.Expr.Call;
import com.zk.jlox.Expr.Get;
import com.zk.jlox.Expr.Grouping;
import com.zk.jlox.Expr.Literal;
import com.zk.jlox.Expr.Logical;
import com.zk.jlox.Expr.Set;
import com.zk.jlox.Expr.This;
import com.zk.jlox.Expr.Unary;
import com.zk.jlox.Expr.Variable;
import com.zk.jlox.Stmt.Block;
import com.zk.jlox.Stmt.Class;
import com.zk.jlox.Stmt.Expression;
import com.zk.jlox.Stmt.Function;
import com.zk.jlox.Stmt.If;
import com.zk.jlox.Stmt.Print;
import com.zk.jlox.Stmt.Return;
import com.zk.jlox.Stmt.Var;
import com.zk.jlox.Stmt.While;

/**
 * 在语法层面上 去检测并优化代码的语义
 * 比如
 *   - 一个变量 先访问 再定义 就是非法操作 这里可以报错出去 不用等到运行时
 *   - 在类中的方法定义时 将 this 添加到语义中
 */
class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currenClass = ClassType.NONE;

    private enum FunctionType {
        NONE,
        FUNCTION,
        METHOD,
        INITIALIZER
    }

    private enum ClassType {
        NONE,
        CLASS
    }

    @Override
    public Void visitThisExpr(This expr) {
        if (currenClass == ClassType.NONE) {
            Jlox.error(expr.keyword, "Can't use this outside of a class.");
            return null;
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitClassStmt(Class stmt) {
        ClassType enclosingClass = currenClass;
        currenClass = ClassType.CLASS;
        declare(stmt.name);
        define(stmt.name);

        if (stmt.superClass != null && stmt.name.lexeme.equals(stmt.superClass.name.lexeme)) {
            Jlox.error(stmt.name, "A class can't inherit from itself.");
        }

        if (stmt.superClass != null) {
            // 如果有继承关系 要检查一下父类是否已定义
            resolve(stmt.superClass);
        }

        beginScope();
        scopes.peek().put("this", true);
        for (Stmt.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.name.lexeme.equals("init")) {
                // 如果 method 名是 init 视为实例的初始化方法 标记一下
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
        }
        endScope();
        currenClass = enclosingClass;
        return null;
    }

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Void visitSetExpr(Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitGetExpr(Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitAssignExpr(Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
          resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            // 变量声明 未定义
            Jlox.error(expr.name, "Can't read local variable in its own initializer. / Variable not init.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    private void resolveLocal(Expr expr, Token name) {
        // 倒序遍历作用域堆栈 由最近的开始
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                // 如果变量在这个作用域中
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        //
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    private void endScope() {
        scopes.pop();
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    void resolve(List<Stmt> statements) {
        for (Stmt stmt: statements) {
            resolve(stmt);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();

        currentFunction = enclosingFunction;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Jlox.error(stmt.keyword, "Can't return from top-level code.");
        }
        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                Jlox.error(stmt.keyword, "Can't return a value from an initializer.");
            }
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        // 先声明
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        // 再定义
        define(stmt.name);
        return null;
    }

    private void declare(Token name) {
        // 标记变量声明
        if (scopes.isEmpty()) {
            return;
        }
        // 变量重复声明检测
        if (scopes.peek().containsKey(name.lexeme)) {
            Jlox.error(name, "Already a variable with this name in this scope.");
        }
        // false 意为尚未准备好
        scopes.peek().put(name.lexeme, false);
    }

    private void define(Token name) {
        // 标记变量定义
        if (scopes.isEmpty()) return;
        // 当变量初始化器解析完成之后 标记变量可用
        scopes.peek().put(name.lexeme, true);
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

}
