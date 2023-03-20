package com.zk.jlox;

import java.util.HashMap;
import java.util.Map;

class JloxInstance {

    private JloxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    public JloxInstance(JloxClass jloxClass) {
        this.klass = jloxClass;
    }

    @Override
    public String toString() {
        return "<instance of " + klass.name + ">";
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }
        throw new RuntimeError(name, "Undefined property '" + name.lexeme +"'.");
    }
}
