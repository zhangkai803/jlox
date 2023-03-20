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
        JloxFunction method = klass.findMethod(name.lexeme);
        if (method != null) {
            return method.bind(this);  // 这里的 this 是 jloxInstance 本身，把方法绑定到实例上
        }
        throw new RuntimeError(name, "Undefined property '" + name.lexeme +"'.");
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }
}
