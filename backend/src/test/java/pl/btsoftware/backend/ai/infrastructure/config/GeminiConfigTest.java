package pl.btsoftware.backend.ai.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeminiConfigTest {

    @BeforeEach
    void setUp() {
        System.setProperty("gemini.enabled", "true");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("GEMINI_API_KEY");
        System.clearProperty("gemini.enabled");
    }

    @Test
    void shouldCreateConfigWithValidApiKey() {
        // given
        var apiKey = "test-api-key-12345";
        System.setProperty("GEMINI_API_KEY", apiKey);

        // when
        var config = new GeminiConfig();

        // then
        assertThat(config.getApiKey()).isEqualTo(apiKey);
        assertThat(config.getModelName()).isEqualTo("gemini-3-flash-preview");
    }

    @Test
    void shouldThrowExceptionWhenApiKeyIsMissing() {
        // given
        assumeTrue(
                System.getenv("GEMINI_API_KEY") == null,
                "Test skipped because GEMINI_API_KEY environment variable is set");
        System.clearProperty("GEMINI_API_KEY");

        // when & then
        assertThatThrownBy(GeminiConfig::new)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GEMINI_API_KEY");
    }

    @Test
    void shouldThrowExceptionWhenApiKeyIsEmpty() {
        // given
        System.setProperty("GEMINI_API_KEY", "");

        // when & then
        assertThatThrownBy(GeminiConfig::new)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GEMINI_API_KEY");
    }

    @Test
    void shouldReadApiKeyFromEnvironmentVariable() {
        // given
        var apiKey = "env-api-key-67890";
        System.setProperty("GEMINI_API_KEY", apiKey);

        // when
        var config = new GeminiConfig();

        // then
        assertThat(config.getApiKey()).isEqualTo(apiKey);
    }

    @Test
    void shouldProvideDefaultModelName() {
        // given
        System.setProperty("GEMINI_API_KEY", "test-key");

        // when
        var config = new GeminiConfig();

        // then
        assertThat(config.getModelName()).isEqualTo("gemini-3-flash-preview");
    }

    @Test
    void shouldProvideDefaultTimeout() {
        // given
        System.setProperty("GEMINI_API_KEY", "test-key");

        // when
        var config = new GeminiConfig();

        // then
        assertThat(config.getTimeoutSeconds()).isEqualTo(30);
    }

    @Test
    void shouldProvideDefaultMaxRetries() {
        // given
        System.setProperty("GEMINI_API_KEY", "test-key");

        // when
        var config = new GeminiConfig();

        // then
        assertThat(config.getMaxRetries()).isEqualTo(3);
    }
}
