package pl.btsoftware.backend.ai.infrastructure.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.ai.infrastructure.config.GeminiConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiClientTest {

    private GeminiConfig config;
    private GeminiClient client;

    @BeforeEach
    void setUp() {
        System.setProperty("gemini.enabled", "true");
        System.setProperty("GEMINI_API_KEY", "test-api-key-12345");
        config = new GeminiConfig();
        client = new GeminiClient(config);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("GEMINI_API_KEY");
        System.clearProperty("gemini.enabled");
    }

    @Test
    void shouldCreateClientWithConfig() {
        // given & when
        var geminiClient = new GeminiClient(config);

        // then
        assertThat(geminiClient).isNotNull();
    }

    @Test
    void shouldProvideGenerateContentMethod() {
        // given
        var prompt = "Test prompt";

        // when & then
        assertThatThrownBy(() -> client.generateContent(prompt))
                .isInstanceOf(GeminiClientException.class);
    }

    @Test
    void shouldThrowExceptionWhenPromptIsNull() {
        // when & then
        assertThatThrownBy(() -> client.generateContent(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Prompt");
    }

    @Test
    void shouldThrowExceptionWhenPromptIsEmpty() {
        // when & then
        assertThatThrownBy(() -> client.generateContent(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Prompt");
    }
}
