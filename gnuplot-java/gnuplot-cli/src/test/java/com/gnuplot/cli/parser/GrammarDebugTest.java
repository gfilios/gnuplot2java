package com.gnuplot.cli.parser;

import com.gnuplot.cli.GnuplotCommandLexer;
import com.gnuplot.cli.GnuplotCommandParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Debug test to understand grammar parsing.
 */
class GrammarDebugTest {

    @Test
    void debugLexer() {
        String input = "plot sin(x)";
        GnuplotCommandLexer lexer = new GnuplotCommandLexer(CharStreams.fromString(input));
        List<? extends Token> tokens = lexer.getAllTokens();

        System.out.println("Input: " + input);
        System.out.println("Tokens:");
        for (Token token : tokens) {
            System.out.printf("  %s: '%s' (type=%d)%n",
                    lexer.getVocabulary().getSymbolicName(token.getType()),
                    token.getText(),
                    token.getType());
        }
    }

    @Test
    void debugParser() {
        String input = "plot sin(x)\n";
        GnuplotCommandLexer lexer = new GnuplotCommandLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GnuplotCommandParser parser = new GnuplotCommandParser(tokens);

        GnuplotCommandParser.ScriptContext tree = parser.script();

        System.out.println("Input: " + input);
        System.out.println("Parse tree:");
        System.out.println(tree.toStringTree(parser));

        if (tree.statement().size() > 0) {
            GnuplotCommandParser.StatementContext stmt = tree.statement(0);
            if (stmt.command() != null && stmt.command().plotCommand() != null) {
                GnuplotCommandParser.PlotCommandContext plotCmd = stmt.command().plotCommand();
                System.out.println("Plot specs: " + plotCmd.plotSpec().size());
                for (GnuplotCommandParser.PlotSpecContext spec : plotCmd.plotSpec()) {
                    System.out.println("  PlotExpression context: " + spec.plotExpression());
                    if (spec.plotExpression() != null) {
                        System.out.println("  PlotExpression text: '" + spec.plotExpression().getText() + "'");
                    }
                    System.out.println("  DataSource context: " + spec.dataSource());
                    if (spec.dataSource() != null) {
                        System.out.println("  DataSource text: '" + spec.dataSource().getText() + "'");
                    }
                }
            }
        }
    }
}
