package org.jlox.primitives;

import org.jlox.Interpreter;
import org.jlox.LoxCallable;

import java.util.List;

public class Clock implements LoxCallable {
    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double)System.currentTimeMillis() / 1000.0;
    }

    @Override
    public String toString() {
        return "<native fn>";
    }
}
