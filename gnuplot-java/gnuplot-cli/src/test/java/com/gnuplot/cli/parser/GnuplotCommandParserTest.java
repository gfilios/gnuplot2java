package com.gnuplot.cli.parser;

import com.gnuplot.cli.command.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GnuplotCommandParser.
 */
class GnuplotCommandParserTest {

    private final GnuplotCommandParser parser = new GnuplotCommandParser();

    @Test
    void parseSetTitleCommand() {
        GnuplotScript script = parser.parse("set title \"My Plot\"");

        assertThat(script.getCommands()).hasSize(1);
        assertThat(script.getCommands().get(0)).isInstanceOf(SetCommand.class);

        SetCommand cmd = (SetCommand) script.getCommands().get(0);
        assertThat(cmd.getOption()).isEqualTo("title");
        assertThat(cmd.getValue()).isEqualTo("My Plot");
    }

    @Test
    void parseSetXLabelCommand() {
        GnuplotScript script = parser.parse("set xlabel \"X Axis\"");

        assertThat(script.getCommands()).hasSize(1);
        SetCommand cmd = (SetCommand) script.getCommands().get(0);
        assertThat(cmd.getOption()).isEqualTo("xlabel");
        assertThat(cmd.getValue()).isEqualTo("X Axis");
    }

    @Test
    void parseSetYLabelCommand() {
        GnuplotScript script = parser.parse("set ylabel \"Y Axis\"");

        assertThat(script.getCommands()).hasSize(1);
        SetCommand cmd = (SetCommand) script.getCommands().get(0);
        assertThat(cmd.getOption()).isEqualTo("ylabel");
        assertThat(cmd.getValue()).isEqualTo("Y Axis");
    }

    @Test
    void parseSetSamplesCommand() {
        GnuplotScript script = parser.parse("set samples 200");

        assertThat(script.getCommands()).hasSize(1);
        SetCommand cmd = (SetCommand) script.getCommands().get(0);
        assertThat(cmd.getOption()).isEqualTo("samples");
        assertThat(cmd.getValue()).isEqualTo(200);
    }

    @Test
    void parseSetGridCommand() {
        GnuplotScript script = parser.parse("set grid");

        assertThat(script.getCommands()).hasSize(1);
        SetCommand cmd = (SetCommand) script.getCommands().get(0);
        assertThat(cmd.getOption()).isEqualTo("grid");
        assertThat(cmd.getValue()).isEqualTo(true);
    }

    @Test
    void parseUnsetGridCommand() {
        GnuplotScript script = parser.parse("unset grid");

        assertThat(script.getCommands()).hasSize(1);
        assertThat(script.getCommands().get(0)).isInstanceOf(UnsetCommand.class);

        UnsetCommand cmd = (UnsetCommand) script.getCommands().get(0);
        assertThat(cmd.getOption()).isEqualTo("grid");
    }

    @Test
    void parsePlotCommandWithSimpleExpression() {
        GnuplotScript script = parser.parse("plot sin(x)");

        assertThat(script.getCommands()).hasSize(1);
        assertThat(script.getCommands().get(0)).isInstanceOf(PlotCommand.class);

        PlotCommand cmd = (PlotCommand) script.getCommands().get(0);
        assertThat(cmd.getPlotSpecs()).hasSize(1);

        PlotCommand.PlotSpec spec = cmd.getPlotSpecs().get(0);
        assertThat(spec.getExpression()).isEqualTo("sin(x)");
        assertThat(spec.getStyle()).isNull();  // No explicit style - executor decides based on data type
    }

    @Test
    void parsePlotCommandWithTitle() {
        GnuplotScript script = parser.parse("plot sin(x) title \"Sine Wave\"");

        PlotCommand cmd = (PlotCommand) script.getCommands().get(0);
        PlotCommand.PlotSpec spec = cmd.getPlotSpecs().get(0);

        assertThat(spec.getExpression()).isEqualTo("sin(x)");
        assertThat(spec.getTitle()).isEqualTo("Sine Wave");
    }

    @Test
    void parsePlotCommandWithStyle() {
        GnuplotScript script = parser.parse("plot sin(x) with points");

        PlotCommand cmd = (PlotCommand) script.getCommands().get(0);
        PlotCommand.PlotSpec spec = cmd.getPlotSpecs().get(0);

        assertThat(spec.getExpression()).isEqualTo("sin(x)");
        assertThat(spec.getStyle()).isEqualTo("points");
    }

    @Test
    void parsePlotCommandWithMultiplePlots() {
        GnuplotScript script = parser.parse("plot sin(x), cos(x)");

        PlotCommand cmd = (PlotCommand) script.getCommands().get(0);
        assertThat(cmd.getPlotSpecs()).hasSize(2);

        assertThat(cmd.getPlotSpecs().get(0).getExpression()).isEqualTo("sin(x)");
        assertThat(cmd.getPlotSpecs().get(1).getExpression()).isEqualTo("cos(x)");
    }

    @Test
    void parsePlotCommandWithComplexExpression() {
        GnuplotScript script = parser.parse("plot x**2 + 2*x + 1");

        PlotCommand cmd = (PlotCommand) script.getCommands().get(0);
        PlotCommand.PlotSpec spec = cmd.getPlotSpecs().get(0);

        assertThat(spec.getExpression()).isEqualTo("x**2+2*x+1");
    }

    @Test
    void parsePauseCommand() {
        GnuplotScript script = parser.parse("pause 2.5 \"Wait for it...\"");

        assertThat(script.getCommands()).hasSize(1);
        assertThat(script.getCommands().get(0)).isInstanceOf(PauseCommand.class);

        PauseCommand cmd = (PauseCommand) script.getCommands().get(0);
        assertThat(cmd.getSeconds()).isEqualTo(2.5);
        assertThat(cmd.getMessage()).isEqualTo("Wait for it...");
    }

    @Test
    void parseResetCommand() {
        GnuplotScript script = parser.parse("reset");

        assertThat(script.getCommands()).hasSize(1);
        assertThat(script.getCommands().get(0)).isInstanceOf(ResetCommand.class);
    }

    @Test
    void parseMultipleCommands() {
        String scriptText = """
                set title "Test Plot"
                set xlabel "X"
                set ylabel "Y"
                set grid
                plot sin(x)
                """;

        GnuplotScript script = parser.parse(scriptText);

        assertThat(script.getCommands()).hasSize(5);
        assertThat(script.getCommands().get(0)).isInstanceOf(SetCommand.class);
        assertThat(script.getCommands().get(1)).isInstanceOf(SetCommand.class);
        assertThat(script.getCommands().get(2)).isInstanceOf(SetCommand.class);
        assertThat(script.getCommands().get(3)).isInstanceOf(SetCommand.class);
        assertThat(script.getCommands().get(4)).isInstanceOf(PlotCommand.class);
    }

    @Test
    void parseScriptWithComments() {
        String scriptText = """
                # This is a comment
                set title "Test"
                # Another comment
                plot sin(x)
                """;

        GnuplotScript script = parser.parse(scriptText);

        // Comments should be skipped
        assertThat(script.getCommands()).hasSize(2);
        assertThat(script.getCommands().get(0)).isInstanceOf(SetCommand.class);
        assertThat(script.getCommands().get(1)).isInstanceOf(PlotCommand.class);
    }

    @Test
    void parseEmptyScript() {
        GnuplotScript script = parser.parse("");

        assertThat(script.getCommands()).isEmpty();
    }

    @Test
    void parseScriptWithBlankLines() {
        String scriptText = """
                set title "Test"


                plot sin(x)
                """;

        GnuplotScript script = parser.parse(scriptText);

        assertThat(script.getCommands()).hasSize(2);
    }

    @Test
    void parseSetKeyLeftBox() {
        String script = "set key left box";
        GnuplotScript result = parser.parse(script);

        assertThat(result.getCommands()).hasSize(1);
        Command cmd = result.getCommands().get(0);
        assertThat(cmd).isInstanceOf(SetCommand.class);

        SetCommand setCmd = (SetCommand) cmd;
        assertThat(setCmd.getOption()).isEqualTo("key");
        assertThat(setCmd.getValue()).isInstanceOf(java.util.Map.class);

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> settings = (java.util.Map<String, Object>) setCmd.getValue();
        // "left" is stored as horizontal component
        assertThat(settings.get("horizontal")).isEqualTo("left");
        assertThat(settings.get("showBorder")).isEqualTo(true);
    }

    @Test
    void parseSetKeyRightNoBox() {
        String script = "set key right nobox";
        GnuplotScript result = parser.parse(script);

        SetCommand setCmd = (SetCommand) result.getCommands().get(0);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> settings = (java.util.Map<String, Object>) setCmd.getValue();
        // "right" is stored as horizontal component
        assertThat(settings.get("horizontal")).isEqualTo("right");
        assertThat(settings.get("showBorder")).isEqualTo(false);
    }

    @Test
    void parseSetKeyBmarginCenterHorizontal() {
        String script = "set key bmargin center horizontal";
        GnuplotScript result = parser.parse(script);

        SetCommand setCmd = (SetCommand) result.getCommands().get(0);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> settings = (java.util.Map<String, Object>) setCmd.getValue();
        // "bmargin" is vertical, "center" is horizontal
        assertThat(settings.get("vertical")).isEqualTo("bmargin");
        assertThat(settings.get("horizontal")).isEqualTo("center");
        // "horizontal" keyword sets layout direction (stored as boolean)
        assertThat(settings.get("layout")).isEqualTo(true);
    }
}
