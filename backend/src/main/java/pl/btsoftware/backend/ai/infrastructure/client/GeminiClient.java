package pl.btsoftware.backend.ai.infrastructure.client;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.btsoftware.backend.ai.infrastructure.config.GeminiConfig;

@Component
@ConditionalOnProperty(name = "gemini.enabled", havingValue = "true")
@Slf4j
public class GeminiClient {
    private final GeminiConfig config;
    private final Client client;

    public GeminiClient(GeminiConfig config) {
        this.config = config;
        this.client = initializeClient();
    }

    private Client initializeClient() {
        log.info("Initializing Gemini API client with model: {}", config.getModelName());
        return Client.builder().apiKey(config.getApiKey()).build();
    }

    @Async
    @Retryable(
            retryFor = {Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000))
    public CompletableFuture<String> generateContent(String prompt) {
        validatePrompt(prompt);

        log.debug("Generating content with Gemini API");

        log.info("Calling Gemini API with prompt: {}", prompt);
        var response = executeGenerateContent(prompt);
        var text = response.text();

        return CompletableFuture.completedFuture(text);
    }

    private GenerateContentResponse executeGenerateContent(String prompt) {
        try {
            return client.models.generateContent(config.getModelName(), prompt, null);
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage());
            throw new GeminiClientException("Error calling Gemini API", e);
        }
    }

    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }
    }
}
