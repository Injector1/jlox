package org.jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, VarState>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;
    private int loopDepth = 0;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.getStatements());
        endScope();
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.getName());
        define(stmt.getName());

        if (stmt.getSuperclass() != null
                && stmt.getName().getLexeme().equals(stmt.getSuperclass().getName().getLexeme())) {
            Lox.error(stmt.getSuperclass().getName(), "A class can't inherit from itself.");
        }

        if (stmt.getSuperclass() != null) {
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.getSuperclass());
        }

        if (stmt.getSuperclass() != null) {
            beginScope();
            VarState superState = new VarState();
            superState.setDefined(true);
            superState.setUsed(false);
            scopes.peek().put("super", superState);
        }

        beginScope();
        VarState state = new VarState();
        state.setDefined(true);
        state.setUsed(false);
        scopes.peek().put("this", state);
        for (Stmt.Function method : stmt.getMethods()) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.getName().getLexeme().equals("init")) {
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
        }
        endScope();

        if (stmt.getSuperclass() != null) {
            endScope();
        }

        for (Stmt.Function method : stmt.getStaticmethods()) {
            FunctionType declaration = FunctionType.FUNCTION;
            resolveFunction(method, declaration);
        }

        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.getName());
        if (stmt.getInitializer() != null) {
            resolve(stmt.getInitializer());
        }
        define(stmt.getName());
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.getName().getLexeme()).isDefined() == Boolean.FALSE) {
            Lox.error(expr.getName(), "Can't read local variable in its own initializer");
        }
        resolveLocal(expr, expr.getName());
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.getValue());
        resolveLocal(expr, expr.getName());
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.getName());
        define(stmt.getName());
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.getCallee());

        for (Expr argument : expr.getArguments()) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.getObject());
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.getExpression());
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.getValue());
        resolve(expr.getObject());
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.getKeyword(), "Can't use 'super' outside of a class");
        } else if (currentClass != ClassType.SUBCLASS) {
            Lox.error(expr.getKeyword(), "Can't use 'super' in a class with no superclass.");
        }
        resolveLocal(expr, expr.getKeyword());
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.getKeyword(), "Can't use 'this' outside of a class.");
            return null;
        } else if (currentClass == ClassType.CLASS && currentFunction != FunctionType.METHOD) {
            Lox.error(expr.getKeyword(), "Cannot use 'this' in static method.");
        }
        resolveLocal(expr, expr.getKeyword());
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitConditionalExpr(Expr.Conditional expr) {
        resolve(expr.getCondition());
        resolve(expr.getThenbranch());
        resolve(expr.getElsebranch());
        return null;
    }

    @Override
    public Void visitAnonFunctionExpr(Expr.AnonFunction expr) {
        resolveParamsAndBody(expr.getParams(), expr.getBody(), FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.getExpression());
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.getCondition());
        resolve(stmt.getThenbranch());
        if (stmt.getElsebranch() != null) {
            resolve(stmt.getElsebranch());
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.getExpression());
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.getKeyword(), "Can't return from top-level code.");
        }
        if (stmt.getValue() != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                Lox.error(stmt.getKeyword(), "Can't return a value from an initializer.");
            }
            resolve(stmt.getValue());
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        loopDepth++;
        resolve(stmt.getCondition());
        resolve(stmt.getBody());
        loopDepth--;
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        if (loopDepth == 0) {
            Lox.error(stmt.getKeyword(), "Can't break from outside a loop.");
        }
        return null;
    }

    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        resolveParamsAndBody(function.getParams(), function.getBody(), type);
    }

    private void resolveParamsAndBody(List<Token> params, List<Stmt> body, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : params) {
            declare(param);
            define(param);
        }
        resolve(body);
        endScope();

        currentFunction = enclosingFunction;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        Map<String, VarState> scope = scopes.pop();

        for (Map.Entry<String, VarState> entry : scope.entrySet()) {
            if (entry.getKey().equals("this") || entry.getKey().equals("super")) {
                continue;
            }
            VarState state = entry.getValue();
            if (state.isDefined() && !state.isUsed()) {
                Lox.error(-1, "Variable '" + entry.getKey() + "' is never used.");
            }
        }
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) {
            return;
        }

        Map<String, VarState> scope = scopes.peek();

        if (scope.containsKey(name.getLexeme())) {
            Lox.error(name, "Already a variable with this name in this scope.");
        }

        VarState state = new VarState();
        state.setDefined(false);
        state.setUsed(false);
        scope.put(name.getLexeme(), state);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        Map<String, VarState> scope = scopes.peek();
        VarState state = scope.get(name.getLexeme());
        state.setDefined(true);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Map<String, VarState> scope = scopes.get(i);
            if (scope.containsKey(name.getLexeme())) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                scope.get(name.getLexeme()).setUsed(true);
                return;
            }
        }
    }

    private static class VarState {
        private boolean defined = false;
        private boolean used = false;

        public boolean isUsed() {
            return used;
        }

        public void setUsed(boolean used) {
            this.used = used;
        }

        public boolean isDefined() {
            return defined;
        }

        public void setDefined(boolean defined) {
            this.defined = defined;
        }
    }
}
