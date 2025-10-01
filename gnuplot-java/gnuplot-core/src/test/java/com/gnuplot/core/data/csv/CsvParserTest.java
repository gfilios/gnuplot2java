package com.gnuplot.core.data.csv;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CsvParser.
 */
class CsvParserTest {

    @Test
    void shouldParseSimpleFields() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("a,b,c");

        assertThat(fields).containsExactly("a", "b", "c");
    }

    @Test
    void shouldParseQuotedFields() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("\"hello\",\"world\"");

        assertThat(fields).containsExactly("hello", "world");
    }

    @Test
    void shouldParseQuotedFieldsWithCommas() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("\"hello, world\",test");

        assertThat(fields).containsExactly("hello, world", "test");
    }

    @Test
    void shouldParseEscapedQuotes() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("\"hello \"\"world\"\"\"");

        assertThat(fields).containsExactly("hello \"world\"");
    }

    @Test
    void shouldParseEscapedCharacters() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("a\\,b,c");

        assertThat(fields).containsExactly("a,b", "c");
    }

    @Test
    void shouldParseEmptyFields() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("a,,c");

        assertThat(fields).containsExactly("a", null, "c");
    }

    @Test
    void shouldParseEmptyLine() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("");

        assertThat(fields).isEmpty();
    }

    @Test
    void shouldTrimFieldsWhenConfigured() {
        CsvConfig config = CsvConfig.builder()
                .trimFields(true)
                .build();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields(" a , b , c ");

        assertThat(fields).containsExactly("a", "b", "c");
    }

    @Test
    void shouldNotTrimFieldsByDefault() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields(" a , b , c ");

        assertThat(fields).containsExactly(" a ", " b ", " c ");
    }

    @Test
    void shouldHandleCustomDelimiter() {
        CsvConfig config = CsvConfig.builder()
                .delimiter(';')
                .build();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("a;b;c");

        assertThat(fields).containsExactly("a", "b", "c");
    }

    @Test
    void shouldHandleTabDelimiter() {
        CsvConfig config = CsvConfig.builder()
                .delimiter('\t')
                .build();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("a\tb\tc");

        assertThat(fields).containsExactly("a", "b", "c");
    }

    @Test
    void shouldHandleMixedQuotedAndUnquoted() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("a,\"b\",c");

        assertThat(fields).containsExactly("a", "b", "c");
    }

    @Test
    void shouldHandleTrailingComma() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("a,b,");

        assertThat(fields).containsExactly("a", "b", null);
    }

    @Test
    void shouldHandleLeadingComma() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields(",a,b");

        assertThat(fields).containsExactly(null, "a", "b");
    }

    @Test
    void shouldHandleNumericValues() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("1.5,2.3,3.7");

        assertThat(fields).containsExactly("1.5", "2.3", "3.7");
    }

    @Test
    void shouldHandleNegativeNumbers() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("-1.5,-2.3,-3.7");

        assertThat(fields).containsExactly("-1.5", "-2.3", "-3.7");
    }

    @Test
    void shouldHandleScientificNotation() {
        CsvConfig config = CsvConfig.defaults();
        CsvParser parser = new CsvParser(config);

        List<String> fields = parser.parseFields("1.5e10,2.3E-5");

        assertThat(fields).containsExactly("1.5e10", "2.3E-5");
    }
}
