package com.gnuplot.core.data.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JsonConfig.
 */
class JsonConfigTest {

    @Test
    void shouldCreateDefaultConfig() {
        JsonConfig config = JsonConfig.defaults();

        assertThat(config.getDataPath()).isEqualTo("$");
        assertThat(config.isFlattenArrays()).isTrue();
        assertThat(config.getArrayName()).isEqualTo("data");
    }

    @Test
    void shouldCreateCustomConfig() {
        JsonConfig config = JsonConfig.builder()
                .dataPath("$.results.data")
                .flattenArrays(false)
                .arrayName("records")
                .build();

        assertThat(config.getDataPath()).isEqualTo("$.results.data");
        assertThat(config.isFlattenArrays()).isFalse();
        assertThat(config.getArrayName()).isEqualTo("records");
    }

    @Test
    void shouldAllowChainedBuilderCalls() {
        JsonConfig config = JsonConfig.builder()
                .dataPath("$.data")
                .flattenArrays(false)
                .build();

        assertThat(config.getDataPath()).isEqualTo("$.data");
        assertThat(config.isFlattenArrays()).isFalse();
    }
}
