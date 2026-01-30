package pl.btsoftware.backend.ai.infrastructure.config;

import lombok.Getter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@ConditionalOnProperty(name = "gemini.enabled", havingValue = "true", matchIfMissing = false)
public class GeminiConfig {
    private final String apiKey;
    private final String modelName;
    private final int timeoutSeconds;
    private final int maxRetries;

    public GeminiConfig() {
        this.apiKey = getApiKeyFromEnvironment();
        this.modelName = "gemini-3-flash-preview";
        this.timeoutSeconds = 30;
        this.maxRetries = 3;
        validateApiKey();
    }

    private String getApiKeyFromEnvironment() {
        return System.getProperty("GEMINI_API_KEY", System.getenv("GEMINI_API_KEY"));
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable or system property must be set");
        }
    }
}
