package com.zk.jlox;

import java.util.HashMap;
import java.util.Map;

class Environment {
    private final Map<String, Object> values = new HashMap<>();

    void print() {
        System.out.println("Env: " + values.toString());
    }

    void define(String name, Object value) {
        values.put(name, value);
        print();
    }

    Object get(Token name) {
        print();
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        throw new RuntimeError(name, "Get variable fail. Undefined variable '" + name.lexeme + "'.");
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            print();
            return;
        }
        throw new RuntimeError(name, "Assign variable fail. Undefined variable '" + name.lexeme + "'.");
    }
}
