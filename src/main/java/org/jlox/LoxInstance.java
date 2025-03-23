package org.jlox;

import org.jlox.exception.RuntimeError;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private final LoxClass clazz;
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass clazz) {
        this.clazz = clazz;
    }

    public Object get(Token name) {
        if (fields.containsKey(name.getLexeme())) {
            return fields.get(name.getLexeme());
        }

        LoxFunction method = clazz.findMethod(name.getLexeme());
        if (method != null) {
            return method.bind(this);
        }
        throw new RuntimeError(name, "Undefined property '" + name.getLexeme() + "'.");
    }

    public void set(Token name, Object value) {
        fields.put(name.getLexeme(), value);
    }

    @Override
    public String toString() {
        return clazz.getName() + " instance";
    }
}
