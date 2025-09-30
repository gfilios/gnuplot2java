package com.gnuplot.core.ast;

import com.gnuplot.core.parser.GnuplotExpressionLexer;
import com.gnuplot.core.parser.GnuplotExpressionParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for AST construction from parsed expressions.
 */
@DisplayName("ASTBuilder")
class ASTBuilderTest {

    @Test
    @DisplayName("should build AST for number literal")
    void shouldBuildNumberLiteral() {
        // Given
        String input = "42";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast).isInstanceOf(NumberLiteral.class);
        NumberLiteral number = (NumberLiteral) ast;
        assertThat(number.value()).isEqualTo(42.0);
    }

    @Test
    @DisplayName("should build AST for variable")
    void shouldBuildVariable() {
        // Given
        String input = "x";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast).isInstanceOf(Variable.class);
        Variable var = (Variable) ast;
        assertThat(var.name()).isEqualTo("x");
    }

    @Test
    @DisplayName("should build AST for simple addition")
    void shouldBuildSimpleAddition() {
        // Given
        String input = "2 + 3";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast).isInstanceOf(BinaryOperation.class);
        BinaryOperation binOp = (BinaryOperation) ast;
        assertThat(binOp.operator()).isEqualTo(BinaryOperation.Operator.ADD);
        assertThat(binOp.left()).isInstanceOf(NumberLiteral.class);
        assertThat(binOp.right()).isInstanceOf(NumberLiteral.class);
    }

    @Test
    @DisplayName("should build AST with correct precedence for 2 + 3 * 4")
    void shouldRespectPrecedence() {
        // Given
        String input = "2 + 3 * 4";

        // When
        ASTNode ast = buildAST(input);

        // Then - Should be (2 + (3 * 4))
        assertThat(ast).isInstanceOf(BinaryOperation.class);
        BinaryOperation add = (BinaryOperation) ast;
        assertThat(add.operator()).isEqualTo(BinaryOperation.Operator.ADD);

        assertThat(add.left()).isInstanceOf(NumberLiteral.class);
        NumberLiteral left = (NumberLiteral) add.left();
        assertThat(left.value()).isEqualTo(2.0);

        assertThat(add.right()).isInstanceOf(BinaryOperation.class);
        BinaryOperation mult = (BinaryOperation) add.right();
        assertThat(mult.operator()).isEqualTo(BinaryOperation.Operator.MULTIPLY);
    }

    @Test
    @DisplayName("should build AST with parentheses")
    void shouldHandleParentheses() {
        // Given
        String input = "(2 + 3) * 4";

        // When
        ASTNode ast = buildAST(input);

        // Then - Should be ((2 + 3) * 4)
        assertThat(ast).isInstanceOf(BinaryOperation.class);
        BinaryOperation mult = (BinaryOperation) ast;
        assertThat(mult.operator()).isEqualTo(BinaryOperation.Operator.MULTIPLY);

        assertThat(mult.left()).isInstanceOf(BinaryOperation.class);
        BinaryOperation add = (BinaryOperation) mult.left();
        assertThat(add.operator()).isEqualTo(BinaryOperation.Operator.ADD);
    }

    @Test
    @DisplayName("should build AST for unary minus")
    void shouldBuildUnaryMinus() {
        // Given
        String input = "-5";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast).isInstanceOf(UnaryOperation.class);
        UnaryOperation unary = (UnaryOperation) ast;
        assertThat(unary.operator()).isEqualTo(UnaryOperation.Operator.NEGATE);
        assertThat(unary.operand()).isInstanceOf(NumberLiteral.class);
    }

    @Test
    @DisplayName("should build AST for function call with no arguments")
    void shouldBuildFunctionCallNoArgs() {
        // Given
        String input = "pi()";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast).isInstanceOf(FunctionCall.class);
        FunctionCall func = (FunctionCall) ast;
        assertThat(func.functionName()).isEqualTo("pi");
        assertThat(func.arguments()).isEmpty();
    }

    @Test
    @DisplayName("should build AST for function call with one argument")
    void shouldBuildFunctionCallOneArg() {
        // Given
        String input = "sin(x)";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast).isInstanceOf(FunctionCall.class);
        FunctionCall func = (FunctionCall) ast;
        assertThat(func.functionName()).isEqualTo("sin");
        assertThat(func.arguments()).hasSize(1);
        assertThat(func.arguments().get(0)).isInstanceOf(Variable.class);
    }

    @Test
    @DisplayName("should build AST for function call with multiple arguments")
    void shouldBuildFunctionCallMultipleArgs() {
        // Given
        String input = "atan2(y, x)";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast).isInstanceOf(FunctionCall.class);
        FunctionCall func = (FunctionCall) ast;
        assertThat(func.functionName()).isEqualTo("atan2");
        assertThat(func.arguments()).hasSize(2);
        assertThat(func.arguments().get(0)).isInstanceOf(Variable.class);
        assertThat(func.arguments().get(1)).isInstanceOf(Variable.class);
    }

    @Test
    @DisplayName("should build AST for nested function calls")
    void shouldBuildNestedFunctionCalls() {
        // Given
        String input = "sin(cos(x))";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast).isInstanceOf(FunctionCall.class);
        FunctionCall sin = (FunctionCall) ast;
        assertThat(sin.functionName()).isEqualTo("sin");
        assertThat(sin.arguments()).hasSize(1);

        assertThat(sin.arguments().get(0)).isInstanceOf(FunctionCall.class);
        FunctionCall cos = (FunctionCall) sin.arguments().get(0);
        assertThat(cos.functionName()).isEqualTo("cos");
    }

    @Test
    @DisplayName("should build AST for power operator")
    void shouldBuildPowerOperator() {
        // Given
        String input = "2 ** 8";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast).isInstanceOf(BinaryOperation.class);
        BinaryOperation power = (BinaryOperation) ast;
        assertThat(power.operator()).isEqualTo(BinaryOperation.Operator.POWER);
    }

    @Test
    @DisplayName("should build AST for comparison operators")
    void shouldBuildComparisonOperators() {
        // Given
        String input = "x < y";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast).isInstanceOf(BinaryOperation.class);
        BinaryOperation comp = (BinaryOperation) ast;
        assertThat(comp.operator()).isEqualTo(BinaryOperation.Operator.LESS_THAN);
    }

    @Test
    @DisplayName("should build AST for logical operators")
    void shouldBuildLogicalOperators() {
        // Given
        String input = "x && y";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast).isInstanceOf(BinaryOperation.class);
        BinaryOperation logical = (BinaryOperation) ast;
        assertThat(logical.operator()).isEqualTo(BinaryOperation.Operator.LOGICAL_AND);
    }

    @Test
    @DisplayName("should build AST for ternary conditional")
    void shouldBuildTernaryConditional() {
        // Given
        String input = "x > 0 ? 1 : -1";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast).isInstanceOf(TernaryConditional.class);
        TernaryConditional ternary = (TernaryConditional) ast;

        assertThat(ternary.condition()).isInstanceOf(BinaryOperation.class);
        assertThat(ternary.trueExpression()).isInstanceOf(NumberLiteral.class);
        assertThat(ternary.falseExpression()).isInstanceOf(UnaryOperation.class);
    }

    @Test
    @DisplayName("should build AST for complex expression")
    void shouldBuildComplexExpression() {
        // Given
        String input = "sin(pi/4) ** 2 + cos(pi/4) ** 2";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast).isInstanceOf(BinaryOperation.class);
        BinaryOperation add = (BinaryOperation) ast;
        assertThat(add.operator()).isEqualTo(BinaryOperation.Operator.ADD);

        // Both sides should be power operations
        assertThat(add.left()).isInstanceOf(BinaryOperation.class);
        assertThat(add.right()).isInstanceOf(BinaryOperation.class);
    }

    @Test
    @DisplayName("should preserve source location")
    void shouldPreserveSourceLocation() {
        // Given
        String input = "42";

        // When
        ASTNode ast = buildAST(input);

        // Then
        assertThat(ast.getLocation()).isNotNull();
        assertThat(ast.getLocation().line()).isGreaterThan(0);
    }

    @Test
    @DisplayName("should build readable toString for AST")
    void shouldBuildReadableToString() {
        // Given
        String input = "2 + 3 * 4";

        // When
        ASTNode ast = buildAST(input);

        // Then
        String str = ast.toString();
        assertThat(str).contains("2");
        assertThat(str).contains("+");
        assertThat(str).contains("3");
        assertThat(str).contains("*");
        assertThat(str).contains("4");
    }

    // Helper method to build AST from input string
    private ASTNode buildAST(String input) {
        GnuplotExpressionLexer lexer = new GnuplotExpressionLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GnuplotExpressionParser parser = new GnuplotExpressionParser(tokens);

        ASTBuilder builder = new ASTBuilder();
        return builder.visit(parser.compilationUnit());
    }
}