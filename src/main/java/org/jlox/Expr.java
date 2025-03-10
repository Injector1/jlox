package org.jlox;

import java.util.List;

abstract class Expr {
	interface Visitor<R> {
		R visitBinaryExpr(Binary expr);
		R visitGroupingExpr(Grouping expr);
		R visitLiteralExpr(Literal expr);
		R visitUnaryExpr(Unary expr);
		R visitConditionalExpr(Conditional expr);
	}

	public static class Binary extends Expr {
		private final Expr left;
		private final Token operator;
		private final Expr right;

		Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpr(this);
		}

		public Expr getLeft() {
			return this.left;
		}

		public Token getOperator() {
			return this.operator;
		}

		public Expr getRight() {
			return this.right;
		}
	}

	public static class Grouping extends Expr {
		private final Expr expression;

		Grouping(Expr expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpr(this);
		}

		public Expr getExpression() {
			return this.expression;
		}
	}

	public static class Literal extends Expr {
		private final Object value;

		Literal(Object value) {
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpr(this);
		}

		public Object getValue() {
			return this.value;
		}
	}

	public static class Unary extends Expr {
		private final Token operator;
		private final Expr right;

		Unary(Token operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpr(this);
		}

		public Token getOperator() {
			return this.operator;
		}

		public Expr getRight() {
			return this.right;
		}
	}

	public static class Conditional extends Expr {
		private final Expr condition;
		private final Expr thenBranch;
		private final Expr elseBranch;

		Conditional(Expr condition, Expr thenBranch, Expr elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitConditionalExpr(this);
		}

		public Expr getCondition() {
			return this.condition;
		}

		public Expr getThenbranch() {
			return this.thenBranch;
		}

		public Expr getElsebranch() {
			return this.elseBranch;
		}
	}


	abstract <R> R accept(Visitor<R> visitor);
}
