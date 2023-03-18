package com.zk.jlox;

class ReturnValue extends RuntimeException {
    final Object value;

    ReturnValue(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
