package com.gnuplot.core.ast;

import com.gnuplot.core.parser.GnuplotExpressionBaseVisitor;
import com.gnuplot.core.parser.GnuplotExpressionParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds an Abstract Syntax Tree (AST) from an ANTLR parse tree.
 *
 * <p>This visitor walks the ANTLR parse tree produced by {@link GnuplotExpressionParser}
 * and converts it into a simplified AST representation that's easier to analyze and evaluate.
 */
public class ASTBuilder extends GnuplotExpressionBaseVisitor<ASTNode> {

    @Override
    public ASTNode visitCompilationUnit(GnuplotExpressionParser.CompilationUnitContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public ASTNode visitExpression(GnuplotExpressionParser.ExpressionContext ctx) {
        return visit(ctx.commaExpression());
    }

    @Override
    public ASTNode visitCommaExpr(GnuplotExpressionParser.CommaExprContext ctx) {
        var operands = ctx.assignmentExpression();
        if (operands.size() == 1) {
            return visit(operands.get(0));
        }

        // Build left-associative chain: (a, b), c
        ASTNode left = visit(operands.get(0));
        for (int i = 1; i < operands.size(); i++) {
            ASTNode right = visit(operands.get(i));
            left = new CommaExpression(left, right, getLocation(ctx));
        }

        return left;
    }

    @Override
    public ASTNode visitAssignExpr(GnuplotExpressionParser.AssignExprContext ctx) {
        String variableName = ctx.IDENTIFIER().getText();
        ASTNode valueExpr = visit(ctx.assignmentExpression());
        return new AssignmentExpression(variableName, valueExpr, getLocation(ctx));
    }

    @Override
    public ASTNode visitAssignTernary(GnuplotExpressionParser.AssignTernaryContext ctx) {
        return visit(ctx.ternaryExpression());
    }

    @Override
    public ASTNode visitTernaryExpression(GnuplotExpressionParser.TernaryExpressionContext ctx) {
        ASTNode condition = visit(ctx.logicalOrExpression());

        if (ctx.ternaryExpression().size() == 2) {
            // This is a ternary conditional: condition ? trueExpr : falseExpr
            ASTNode trueExpr = visit(ctx.ternaryExpression(0));
            ASTNode falseExpr = visit(ctx.ternaryExpression(1));

            return new TernaryConditional(
                    condition,
                    trueExpr,
                    falseExpr,
                    getLocation(ctx)
            );
        }

        return condition;
    }

    @Override
    public ASTNode visitLogicalOrExpression(GnuplotExpressionParser.LogicalOrExpressionContext ctx) {
        return buildBinaryChain(ctx.logicalAndExpression(), ctx.OR(), BinaryOperation.Operator.LOGICAL_OR, ctx);
    }

    @Override
    public ASTNode visitLogicalAndExpression(GnuplotExpressionParser.LogicalAndExpressionContext ctx) {
        return buildBinaryChain(ctx.bitwiseOrExpression(), ctx.AND(), BinaryOperation.Operator.LOGICAL_AND, ctx);
    }

    @Override
    public ASTNode visitBitwiseOrExpression(GnuplotExpressionParser.BitwiseOrExpressionContext ctx) {
        return buildBinaryChain(ctx.bitwiseXorExpression(), ctx.BITOR(), BinaryOperation.Operator.BITWISE_OR, ctx);
    }

    @Override
    public ASTNode visitBitwiseXorExpression(GnuplotExpressionParser.BitwiseXorExpressionContext ctx) {
        return buildBinaryChain(ctx.bitwiseAndExpression(), ctx.BITXOR(), BinaryOperation.Operator.BITWISE_XOR, ctx);
    }

    @Override
    public ASTNode visitBitwiseAndExpression(GnuplotExpressionParser.BitwiseAndExpressionContext ctx) {
        return buildBinaryChain(ctx.equalityExpression(), ctx.BITAND(), BinaryOperation.Operator.BITWISE_AND, ctx);
    }

    @Override
    public ASTNode visitEqualityExpression(GnuplotExpressionParser.EqualityExpressionContext ctx) {
        var operands = ctx.relationalExpression();
        if (operands.size() == 1) {
            return visit(operands.get(0));
        }

        ASTNode left = visit(operands.get(0));
        for (int i = 1; i < operands.size(); i++) {
            TerminalNode op = i - 1 < ctx.EQ().size() ? ctx.EQ(i - 1) : ctx.NE(i - 1 - ctx.EQ().size());
            BinaryOperation.Operator operator = op.getSymbol().getType() == GnuplotExpressionParser.EQ
                    ? BinaryOperation.Operator.EQUAL
                    : BinaryOperation.Operator.NOT_EQUAL;

            ASTNode right = visit(operands.get(i));
            left = new BinaryOperation(operator, left, right, getLocation(ctx));
        }

        return left;
    }

    @Override
    public ASTNode visitRelationalExpression(GnuplotExpressionParser.RelationalExpressionContext ctx) {
        var operands = ctx.additiveExpression();
        if (operands.size() == 1) {
            return visit(operands.get(0));
        }

        ASTNode left = visit(operands.get(0));
        for (int i = 1; i < operands.size(); i++) {
            BinaryOperation.Operator operator;
            if (i - 1 < ctx.LT().size()) {
                operator = BinaryOperation.Operator.LESS_THAN;
            } else if (i - 1 - ctx.LT().size() < ctx.LE().size()) {
                operator = BinaryOperation.Operator.LESS_EQUAL;
            } else if (i - 1 - ctx.LT().size() - ctx.LE().size() < ctx.GT().size()) {
                operator = BinaryOperation.Operator.GREATER_THAN;
            } else {
                operator = BinaryOperation.Operator.GREATER_EQUAL;
            }

            ASTNode right = visit(operands.get(i));
            left = new BinaryOperation(operator, left, right, getLocation(ctx));
        }

        return left;
    }

    @Override
    public ASTNode visitAdditiveExpression(GnuplotExpressionParser.AdditiveExpressionContext ctx) {
        var operands = ctx.multiplicativeExpression();
        if (operands.size() == 1) {
            return visit(operands.get(0));
        }

        ASTNode left = visit(operands.get(0));
        for (int i = 1; i < operands.size(); i++) {
            BinaryOperation.Operator operator = i - 1 < ctx.PLUS().size()
                    ? BinaryOperation.Operator.ADD
                    : BinaryOperation.Operator.SUBTRACT;

            ASTNode right = visit(operands.get(i));
            left = new BinaryOperation(operator, left, right, getLocation(ctx));
        }

        return left;
    }

    @Override
    public ASTNode visitMultiplicativeExpression(GnuplotExpressionParser.MultiplicativeExpressionContext ctx) {
        var operands = ctx.powerExpression();
        if (operands.size() == 1) {
            return visit(operands.get(0));
        }

        ASTNode left = visit(operands.get(0));
        for (int i = 1; i < operands.size(); i++) {
            BinaryOperation.Operator operator;
            if (i - 1 < ctx.STAR().size()) {
                operator = BinaryOperation.Operator.MULTIPLY;
            } else if (i - 1 - ctx.STAR().size() < ctx.SLASH().size()) {
                operator = BinaryOperation.Operator.DIVIDE;
            } else {
                operator = BinaryOperation.Operator.MODULO;
            }

            ASTNode right = visit(operands.get(i));
            left = new BinaryOperation(operator, left, right, getLocation(ctx));
        }

        return left;
    }

    @Override
    public ASTNode visitPowerExpression(GnuplotExpressionParser.PowerExpressionContext ctx) {
        return buildBinaryChain(ctx.unaryExpression(), ctx.POW(), BinaryOperation.Operator.POWER, ctx);
    }

    @Override
    public ASTNode visitUnaryMinus(GnuplotExpressionParser.UnaryMinusContext ctx) {
        ASTNode operand = visit(ctx.unaryExpression());
        return new UnaryOperation(UnaryOperation.Operator.NEGATE, operand, getLocation(ctx));
    }

    @Override
    public ASTNode visitUnaryPlus(GnuplotExpressionParser.UnaryPlusContext ctx) {
        ASTNode operand = visit(ctx.unaryExpression());
        return new UnaryOperation(UnaryOperation.Operator.PLUS, operand, getLocation(ctx));
    }

    @Override
    public ASTNode visitLogicalNot(GnuplotExpressionParser.LogicalNotContext ctx) {
        ASTNode operand = visit(ctx.unaryExpression());
        return new UnaryOperation(UnaryOperation.Operator.LOGICAL_NOT, operand, getLocation(ctx));
    }

    @Override
    public ASTNode visitBitwiseNot(GnuplotExpressionParser.BitwiseNotContext ctx) {
        ASTNode operand = visit(ctx.unaryExpression());
        return new UnaryOperation(UnaryOperation.Operator.BITWISE_NOT, operand, getLocation(ctx));
    }

    @Override
    public ASTNode visitUnaryPostfix(GnuplotExpressionParser.UnaryPostfixContext ctx) {
        return visit(ctx.postfixExpression());
    }

    @Override
    public ASTNode visitFunctionCall(GnuplotExpressionParser.FunctionCallContext ctx) {
        String functionName = ctx.IDENTIFIER().getText();
        List<ASTNode> arguments = new ArrayList<>();

        if (ctx.argumentList() != null) {
            for (GnuplotExpressionParser.AssignmentExpressionContext exprCtx : ctx.argumentList().assignmentExpression()) {
                arguments.add(visit(exprCtx));
            }
        }

        return new FunctionCall(functionName, arguments, getLocation(ctx));
    }

    @Override
    public ASTNode visitPrimary(GnuplotExpressionParser.PrimaryContext ctx) {
        return visit(ctx.primaryExpression());
    }

    @Override
    public ASTNode visitNumberLiteral(GnuplotExpressionParser.NumberLiteralContext ctx) {
        double value = Double.parseDouble(ctx.NUMBER().getText());
        return new NumberLiteral(value, getLocation(ctx));
    }

    @Override
    public ASTNode visitVariable(GnuplotExpressionParser.VariableContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        return new Variable(name, getLocation(ctx));
    }

    @Override
    public ASTNode visitParenthesizedExpr(GnuplotExpressionParser.ParenthesizedExprContext ctx) {
        return visit(ctx.expression());
    }

    // Helper methods

    /**
     * Builds a left-associative chain of binary operations.
     */
    private ASTNode buildBinaryChain(
            List<? extends ParserRuleContext> operands,
            List<TerminalNode> operators,
            BinaryOperation.Operator operator,
            ParserRuleContext ctx
    ) {
        if (operands.size() == 1) {
            return visit(operands.get(0));
        }

        ASTNode left = visit(operands.get(0));
        for (int i = 1; i < operands.size(); i++) {
            ASTNode right = visit(operands.get(i));
            left = new BinaryOperation(operator, left, right, getLocation(ctx));
        }

        return left;
    }

    /**
     * Extracts source location from a parse tree context.
     */
    private SourceLocation getLocation(ParserRuleContext ctx) {
        if (ctx.start == null) {
            return SourceLocation.UNKNOWN;
        }

        return new SourceLocation(
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine(),
                ctx.start.getStartIndex(),
                ctx.stop != null ? ctx.stop.getStopIndex() : ctx.start.getStopIndex()
        );
    }
}