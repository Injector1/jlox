package org.jlox;

import org.jlox.exception.RuntimeError;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    private final String name;
    private final LoxClass superclass;
    private final Map<String, LoxFunction> methods;
    private final Map<String, LoxFunction> staticMethods;

    public LoxClass(String name,
                    LoxClass superclass,
                    Map<String, LoxFunction> methods,
                    Map<String, LoxFunction> staticMethods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
        this.staticMethods = staticMethods;
    }

    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }
        return null;
    }

    public LoxFunction findStaticMethod(String name) {
        if (staticMethods.containsKey(name)) {
            return staticMethods.get(name);
        }

        if (superclass != null) {
            return superclass.findStaticMethod(name);
        }
        return null;
    }

    public Object getStatic(Token name) {
        LoxFunction method = findStaticMethod(name.getLexeme());
        if (method != null) {
            return method;
        }
        throw new RuntimeError(name, "Undefined static property '" + name.getLexeme() + "'.");
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null) {
            return 0;
        }
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    public String getName() {
        return name;
    }

    public LoxClass getSuperclass() {
        return superclass;
    }
}
