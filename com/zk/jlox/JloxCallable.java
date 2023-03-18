package com.zk.jlox;

import java.util.List;

interface JloxCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
