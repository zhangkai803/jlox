package com.zk.jlox;

import java.util.List;

class JloxClass implements JloxCallable {

    final String name;

    public JloxClass(String name) {
        this.name = name;
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

}
