package org.jlox;

import java.util.List;

abstract class Stmt {
	interface Visitor<R> {
		R visitBlockStmt(Block stmt);
		R visitExpressionStmt(Expression stmt);
		R visitPrintStmt(Print stmt);
		R visitVarStmt(Var stmt);
	}

	public static class Block extends Stmt {
		private final List<Stmt> statements;

		Block(List<Stmt> statements) {
			this.statements = statements;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStmt(this);
		}

		public List<Stmt> getStatements() {
			return this.statements;
		}
	}

	public static class Expression extends Stmt {
		private final Expr expression;

		Expression(Expr expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}

		public Expr getExpression() {
			return this.expression;
		}
	}

	public static class Print extends Stmt {
		private final Expr expression;

		Print(Expr expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
		}

		public Expr getExpression() {
			return this.expression;
		}
	}

	public static class Var extends Stmt {
		private final Token name;
		private final Expr initializer;

		Var(Token name, Expr initializer) {
			this.name = name;
			this.initializer = initializer;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVarStmt(this);
		}

		public Token getName() {
			return this.name;
		}

		public Expr getInitializer() {
			return this.initializer;
		}
	}


	abstract <R> R accept(Visitor<R> visitor);
}
