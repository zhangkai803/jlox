package com.zk.jlox;

class JloxClass {

    final String name;

    public JloxClass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "<class " + this.name + ">";
    }

}
