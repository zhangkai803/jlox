package com.zk.jlox;

import java.util.HashMap;
import java.util.Map;

class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment environment) {
        this.enclosing = environment;
    }

    void print() {
        System.out.println("Env: " + values.toString());
    }

    void define(String name, Object value) {
        // 声明变量
        values.put(name, value);
        print();
    }

    Object get(Token name) {
        // 读取变量
        print();
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null) {
            return enclosing.get(name);
        }
        throw new RuntimeError(name, "Get variable fail. Undefined variable '" + name.lexeme + "'.");
    }

    public void assign(Token name, Object value) {
        // 修改变量
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            print();
            return;
        }
        if (enclosing != null) {
            // 如果变量不在当前作用域 也需要抛给上层
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Assign variable fail. Undefined variable '" + name.lexeme + "'.");
    }
}
