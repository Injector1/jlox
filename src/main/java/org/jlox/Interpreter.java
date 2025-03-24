package org.jlox;

import org.jlox.exception.BreakError;
import org.jlox.exception.Return;
import org.jlox.exception.RuntimeError;
import org.jlox.primitives.Clock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();


    Interpreter() {
        globals.define("clock", new Clock());
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    public Object evaluateExpression(Stmt.Expression stmt) {
        return evaluate(stmt.getExpression());
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.getValue());

        Integer distance = locals.get(expr);

        if (distance != null) {
            environment.assignAt(distance, expr.getName(), value);
        } else {
            globals.assign(expr.getName(), value);
        }
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        if (expr.getOperator().getType() == TokenType.COMMA) {
            evaluate(expr.getLeft());
            return evaluate(expr.getRight());
        }

        Object left = evaluate(expr.getLeft());
        Object right = evaluate(expr.getRight());

        switch (expr.getOperator().getType()) {
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
            case GREATER -> {
                if (left instanceof Double && right instanceof Double) {
                    return ((double) left) > ((double) right);
                } else if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo(((String) right)) > 0;
                }
                throw new RuntimeError(
                        expr.getOperator(),
                        "Operands for '>' must be both numbers or both strings."
                );
            }
            case GREATER_EQUAL -> {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left >= (double) right;
                } else if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) >= 0;
                } else {
                    throw new RuntimeError(
                            expr.getOperator(),
                            "Operands for '>=' must be both numbers or both strings."
                    );
                }
            }
            case LESS -> {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left < (double) right;
                } else if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) < 0;
                } else {
                    throw new RuntimeError(
                            expr.getOperator(),
                            "Operands for '<' must be both numbers or both strings."
                    );
                }
            }
            case LESS_EQUAL -> {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left <= (double) right;
                } else if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) <= 0;
                } else {
                    throw new RuntimeError(
                            expr.getOperator(),
                            "Operands for '<=' must be both numbers or both strings."
                    );
                }
            }
            case MINUS -> {
                checkNumberOperands(expr.getOperator(), left, right);
                return (double) left - (double) right;
            }
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                } else if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }
                throw new RuntimeError(
                        expr.getOperator(),
                        "Operands must be two numbers or two strings"
                );
            }
            case SLASH -> {
                checkNumberOperands(expr.getOperator(), left, right);
                if ((double) right == 0.0) {
                    throw new RuntimeError(expr.getOperator(), "Division by zero");
                }
                return (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperands(expr.getOperator(), left, right);
                return (double) left * (double) right;
            }
        }
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.getCallee());

        List<Object> arguments = new ArrayList<>();

        for (Expr argument : expr.getArguments()) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.getParen(), "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable) callee;

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(
                    expr.getParen(),
                    "Expected " + function.arity() + " arguments but got "
                            + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.getObject());
        if (object instanceof LoxInstance) {
            return ((LoxInstance) object).get(expr.getName());
        }
        if (object instanceof LoxClass) {
            return ((LoxClass) object).getStatic(expr.getName());
        }
        throw new RuntimeError(expr.getName(), "Only instances have properties.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.getExpression());
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.getValue();
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.getLeft());

        if (expr.getOperator().getType() == TokenType.OR) {
            if (isTruthy(left)) {
                return left;
            }
        } else {
            if (!isTruthy(left)) {
                return left;
            }
        }
        return evaluate(expr.getRight());
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.getObject());

        if (!(object instanceof LoxInstance)) {
            throw new RuntimeError(expr.getName(), "Only instances have fields.");
        }

        Object value = evaluate(expr.getValue());
        ((LoxInstance) object).set(expr.getName(), value);
        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        Integer distance = locals.get(expr);
        LoxClass superClass = (LoxClass) environment.getAt(distance, "super");

        LoxInstance object = (LoxInstance) environment.getAt(distance - 1, "this");

        LoxFunction method = superClass.findMethod(expr.getMethod().getLexeme());

        if (method == null) {
            throw new RuntimeError(
                    expr.getMethod(),
                    "Undefined property '" + expr.getMethod().getLexeme() + "'."
            );
        }
        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.getKeyword(), expr);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.getRight());

        switch (expr.getOperator().getType()) {
            case MINUS -> {
                checkNumberOperand(expr.getOperator(), right);
                return -(double) right;
            }
            case BANG -> {
                return !isTruthy(right);
            }
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.getName(), expr);
    }

    @Override
    public Object visitConditionalExpr(Expr.Conditional expr) {
        Object condition = evaluate(expr.getCondition());

        if (isTruthy(condition)) {
            return evaluate(expr.getThenbranch());
        }
        return evaluate(expr.getElsebranch());
    }

    @Override
    public Object visitAnonFunctionExpr(Expr.AnonFunction expr) {
        Token dummyName = new Token(TokenType.IDENTIFIER, "<anon>", null, -1);
        Stmt.Function functionStmt = new Stmt.Function(dummyName, expr.getParams(), expr.getBody());
        return new LoxFunction(functionStmt, environment, false);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.getStatements(), new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.getSuperclass() != null) {
            superclass = evaluate(stmt.getSuperclass());
            if (!(superclass instanceof LoxClass)) {
                throw new RuntimeError(stmt.getSuperclass().getName(), "Superclass must be a class.");
            }
        }
        environment.define(stmt.getName().getLexeme(), null);

        if (stmt.getSuperclass() != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        Map<String, LoxFunction> methods = new HashMap<>();
        Map<String, LoxFunction> staticMethods = new HashMap<>();


        for (Stmt.Function method : stmt.getMethods()) {
            LoxFunction function = new LoxFunction(method, environment, method.getName().getLexeme().equals("init"));
            methods.put(method.getName().getLexeme(), function);
        }

        for (Stmt.Function staticmethod : stmt.getStaticmethods()) {
            LoxFunction function = new LoxFunction(staticmethod, environment, false);
            staticMethods.put(staticmethod.getName().getLexeme(), function);
        }

        LoxClass loxClass = new LoxClass(stmt.getName().getLexeme(), (LoxClass) superclass, methods, staticMethods);

        if (superclass != null) {
            environment = environment.getEnclosing();
        }

        environment.assign(stmt.getName(), loxClass);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.getExpression());
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment, false);
        environment.define(stmt.getName().getLexeme(), function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.getCondition()))) {
            execute(stmt.getThenbranch());
        } else if (stmt.getElsebranch() != null) {
            execute(stmt.getElsebranch());
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.getExpression());
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.getValue() != null) {
            value = evaluate(stmt.getValue());
        }
        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.getInitializer() != null) {
            value = evaluate(stmt.getInitializer());
        }
        environment.define(stmt.getName().getLexeme(), value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        try {
            while (isTruthy(evaluate(stmt.getCondition()))) {
                try {
                    execute(stmt.getBody());
                } catch (BreakError breakErr) {
                    break;
                }
            }
        } catch (BreakError breakErr) {
            // ignore
        }

        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakError();
    }

    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.getLexeme());
        }
        return globals.get(name);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object) {
        return Boolean.TRUE.equals(
                Optional.ofNullable(object)
                    .map(o -> {
                        if (o instanceof Boolean) {
                            return (boolean) o;
                        }
                        return true;
                    }).orElse(null)
        );
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    String stringify(Object object) {
        if (object == null) {
            return "nil";
        }

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    public Environment getGlobals() {
        return globals;
    }
}
