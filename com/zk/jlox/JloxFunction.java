package com.zk.jlox;

import java.util.List;

class JloxFunction implements JloxCallable {

    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    JloxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // 每个函数调用都要创建自己的 env
        Environment environment = new Environment(closure);
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
            if (isInitializer) {
                // 如果当前方法是实例的初始化方法 只能返回 this
                return closure.getAt(0, "this");
            }
            return value.value;
        }

        // 走到这里说明 函数中没有 return 语句
        if (isInitializer) {
            // 如果当前函数 是某个类的初始化方法 返回 this
            return closure.getAt(0, "this");
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fun " + declaration.name.lexeme + ">";
    }

    public JloxFunction bind(JloxInstance jloxInstance) {
        Environment environment = new Environment(closure);
        environment.define("this", jloxInstance);
        return new JloxFunction(declaration, environment, isInitializer);
    }

}
