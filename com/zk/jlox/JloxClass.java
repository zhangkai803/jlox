package com.zk.jlox;

import java.util.List;
import java.util.Map;

class JloxClass implements JloxCallable {

    final String name;
    private final Map<String, JloxFunction> methods;

    public JloxClass(String name, Map<String, JloxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        JloxInstance instance = new JloxInstance(this);
        return instance;
    }

    @Override
    public String toString() {
        return "<class " + this.name + ">";
    }

    public JloxFunction findMethod(String lexeme) {
        if (methods.containsKey(lexeme)) {
            return methods.get(lexeme);
        }
        return null;
    }

}
