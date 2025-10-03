package com.gnuplot.cli.parser;

import com.gnuplot.cli.GnuplotCommandLexer;
import com.gnuplot.cli.command.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for Gnuplot command scripts.
 */
public class GnuplotCommandParser {

    /**
     * Parse a Gnuplot script and return a list of commands.
     */
    public GnuplotScript parse(String scriptContent) {
        // Create lexer and parser
        GnuplotCommandLexer lexer = new GnuplotCommandLexer(CharStreams.fromString(scriptContent));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        com.gnuplot.cli.GnuplotCommandParser parser = new com.gnuplot.cli.GnuplotCommandParser(tokens);

        // Parse the script
        ParseTree tree = parser.script();

        // Convert parse tree to command objects
        CommandBuilderVisitor visitor = new CommandBuilderVisitor();
        List<Command> commands = visitor.visit(tree);

        return new GnuplotScript(commands);
    }
}
