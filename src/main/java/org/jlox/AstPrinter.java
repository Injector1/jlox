package org.jlox;

public class AstPrinter implements Expr.Visitor<String> {
    public static void main(String[] args) {
        Expr.Binary expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)
                )
        );

        System.out.println(new AstPrinter().print(expression));
    }

    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        // TODO: implement
        return "";
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.getOperator().getLexeme(), expr.getLeft(), expr.getRight());
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        // TODO: implement this
        return "";
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        // TODO: implement this
        return "";
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.getExpression());
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.getValue() == null) {
            return "nil";
        }
        return expr.getValue().toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        // TODO: implement this
        return "";
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.getOperator().getLexeme(), expr.getRight());
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        // TODO: implement this
        return "";
    }

    @Override
    public String visitConditionalExpr(Expr.Conditional expr) {
        return parenthesize("?:", expr.getCondition(), expr.getThenbranch(), expr.getElsebranch());
    }

    @Override
    public String visitAnonFunctionExpr(Expr.AnonFunction expr) {
        // TODO: implement this
        return "";
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("(").append(name);

        for (Expr expr : exprs) {
            stringBuilder.append(" ");
            stringBuilder.append(expr.accept(this));
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }
}
