package org.jlox.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output_directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Get      : Expr object, Token name",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Logical  : Expr left, Token operator, Expr right",
                "Set      : Expr object, Token name, Expr value",
                "Unary    : Token operator, Expr right",
                "Variable : Token name",
                "Conditional: Expr condition, Expr thenBranch, Expr elseBranch",
                "AnonFunction: List<Token> params, List<Stmt> body"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block: List<Stmt> statements",
                "Class: Token name, List<Stmt.Function> methods",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> params," +
                            " List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "Return     : Token keyword, Expr value",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt body",
                "Break      : Token keyword"
        ));
    }

    private static void defineAst(String outputDir,
                                  String baseName,
                                  List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter printWriter = new PrintWriter(path, StandardCharsets.UTF_8);

        printWriter.println("package org.jlox;");
        printWriter.println();
        printWriter.println("import java.util.List;");
        printWriter.println();
        printWriter.println("abstract class " + baseName + " {");

        defineVisitor(printWriter, baseName, types);
        printWriter.println();

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(printWriter, baseName, className, fields);
        }

        printWriter.println();
        printWriter.println("\tabstract <R> R accept(Visitor<R> visitor);");

        printWriter.println("}");
        printWriter.close();
    }

    private static void defineType(PrintWriter writer,
                                   String baseName,
                                   String className,
                                   String fieldList) {
        writer.println("\tpublic static class " + className + " extends " + baseName + " {");

        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            writer.println("\t\tprivate final " + field + ";");
        }
        writer.println();

        writer.println("\t\t" + className + "(" + fieldList + ") {");

        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("\t\t\tthis." + name + " = " + name + ";");
        }
        writer.println("\t\t}");

        writer.println();
        writer.println("\t\t@Override");
        writer.println("\t\t<R> R accept(Visitor<R> visitor) {");
        writer.println("\t\t\treturn visitor.visit" + className + baseName + "(this);");
        writer.println("\t\t}");

        for (String field : fields) {
            String[] splittedField = field.split(" ");
            String fieldType = splittedField[0];
            String fieldName = splittedField[1];
            writer.println();
            writer.println("\t\t" + createGetterName(fieldType, fieldName) + "() {");
            writer.println("\t\t\treturn this." + fieldName + ";");
            writer.println("\t\t}");
        }

        writer.println("\t}");
        writer.println();
    }

    private static void defineVisitor(PrintWriter writer,
                                      String baseName,
                                      List<String> types) {
        writer.println("\tinterface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("\t\tR visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println("\t}");
    }

    private static String createGetterName(String fieldType, String fieldName) {
        return "public "
                + fieldType
                + " get"
                + fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1).toLowerCase();
    }
}
