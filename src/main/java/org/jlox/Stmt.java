package org.jlox;

import java.util.List;

abstract class Stmt {
	interface Visitor<R> {
		R visitBlockStmt(Block stmt);
		R visitClassStmt(Class stmt);
		R visitExpressionStmt(Expression stmt);
		R visitFunctionStmt(Function stmt);
		R visitIfStmt(If stmt);
		R visitPrintStmt(Print stmt);
		R visitReturnStmt(Return stmt);
		R visitVarStmt(Var stmt);
		R visitWhileStmt(While stmt);
		R visitBreakStmt(Break stmt);
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

	public static class Class extends Stmt {
		private final Token name;
		private final Expr.Variable superclass;
		private final List<Stmt.Function> methods;
		private final List<Stmt.Function> staticMethods;

		Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods, List<Stmt.Function> staticMethods) {
			this.name = name;
			this.superclass = superclass;
			this.methods = methods;
			this.staticMethods = staticMethods;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitClassStmt(this);
		}

		public Token getName() {
			return this.name;
		}

		public Expr.Variable getSuperclass() {
			return this.superclass;
		}

		public List<Stmt.Function> getMethods() {
			return this.methods;
		}

		public List<Stmt.Function> getStaticmethods() {
			return this.staticMethods;
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

	public static class Function extends Stmt {
		private final Token name;
		private final List<Token> params;
		private final List<Stmt> body;

		Function(Token name, List<Token> params, List<Stmt> body) {
			this.name = name;
			this.params = params;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitFunctionStmt(this);
		}

		public Token getName() {
			return this.name;
		}

		public List<Token> getParams() {
			return this.params;
		}

		public List<Stmt> getBody() {
			return this.body;
		}
	}

	public static class If extends Stmt {
		private final Expr condition;
		private final Stmt thenBranch;
		private final Stmt elseBranch;

		If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStmt(this);
		}

		public Expr getCondition() {
			return this.condition;
		}

		public Stmt getThenbranch() {
			return this.thenBranch;
		}

		public Stmt getElsebranch() {
			return this.elseBranch;
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

	public static class Return extends Stmt {
		private final Token keyword;
		private final Expr value;

		Return(Token keyword, Expr value) {
			this.keyword = keyword;
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitReturnStmt(this);
		}

		public Token getKeyword() {
			return this.keyword;
		}

		public Expr getValue() {
			return this.value;
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

	public static class While extends Stmt {
		private final Expr condition;
		private final Stmt body;

		While(Expr condition, Stmt body) {
			this.condition = condition;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}

		public Expr getCondition() {
			return this.condition;
		}

		public Stmt getBody() {
			return this.body;
		}
	}

	public static class Break extends Stmt {
		private final Token keyword;

		Break(Token keyword) {
			this.keyword = keyword;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBreakStmt(this);
		}

		public Token getKeyword() {
			return this.keyword;
		}
	}


	abstract <R> R accept(Visitor<R> visitor);
}
