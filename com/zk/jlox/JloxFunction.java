package com.zk.jlox;

import java.util.List;

class JloxFunction implements JloxCallable {

    private final Stmt.Function declaration;

    JloxFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // 每个函数调用都要创建自己的 env
        Environment environment = new Environment(interpreter.globals);
        // 先将函数的入参注入到当前作用域中
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }
        // 解释器执行函数代码块
        // 其中会操作 env 的替换与恢复
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (ReturnValue value) {
            // 把返回值以异常的形式抛出
            return value.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fun " + declaration.name.lexeme + ">";
    }

}
