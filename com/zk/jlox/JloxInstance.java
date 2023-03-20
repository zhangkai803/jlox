package com.zk.jlox;

class JloxInstance {

    private JloxClass klass;

    public JloxInstance(JloxClass jloxClass) {
        this.klass = jloxClass;
    }

    @Override
    public String toString() {
        return "<instance of " + klass.name + ">";
    }
}
