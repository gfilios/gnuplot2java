package com.gnuplot.core.data.csv;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for CsvConfig.
 */
class CsvConfigTest {

    @Test
    void shouldCreateDefaultConfig() {
        CsvConfig config = CsvConfig.defaults();

        assertThat(config.getDelimiter()).isEqualTo(',');
        assertThat(config.getQuoteChar()).isEqualTo('"');
        assertThat(config.getEscapeChar()).isEqualTo('\\');
        assertThat(config.hasHeader()).isTrue();
        assertThat(config.isSkipEmptyLines()).isTrue();
        assertThat(config.isTrimFields()).isFalse();
        assertThat(config.getSkipLines()).isEqualTo(0);
        assertThat(config.getCommentPrefix()).isEqualTo("#");
    }

    @Test
    void shouldCreateCustomConfig() {
        CsvConfig config = CsvConfig.builder()
                .delimiter(';')
                .quoteChar('\'')
                .escapeChar('\\')
                .hasHeader(false)
                .skipEmptyLines(false)
                .trimFields(true)
                .skipLines(2)
                .commentPrefix("//")
                .build();

        assertThat(config.getDelimiter()).isEqualTo(';');
        assertThat(config.getQuoteChar()).isEqualTo('\'');
        assertThat(config.getEscapeChar()).isEqualTo('\\');
        assertThat(config.hasHeader()).isFalse();
        assertThat(config.isSkipEmptyLines()).isFalse();
        assertThat(config.isTrimFields()).isTrue();
        assertThat(config.getSkipLines()).isEqualTo(2);
        assertThat(config.getCommentPrefix()).isEqualTo("//");
    }

    @Test
    void shouldThrowOnNegativeSkipLines() {
        assertThatThrownBy(() -> CsvConfig.builder().skipLines(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    void shouldAllowChainedBuilderCalls() {
        CsvConfig config = CsvConfig.builder()
                .delimiter(';')
                .hasHeader(false)
                .trimFields(true)
                .build();

        assertThat(config.getDelimiter()).isEqualTo(';');
        assertThat(config.hasHeader()).isFalse();
        assertThat(config.isTrimFields()).isTrue();
    }
}
