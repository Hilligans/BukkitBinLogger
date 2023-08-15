package dev.hilligans.binlogger.util;

public class Tuple<T, Q> {

    public T t;
    public Q q;

    public Tuple(T t, Q q) {
        this.t = t;
        this.q = q;
    }
}
