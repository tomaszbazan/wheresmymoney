package pl.btsoftware.backend.configuration;

import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import pl.btsoftware.backend.ai.infrastructure.client.GeminiClient;
import pl.btsoftware.backend.ai.infrastructure.config.GeminiConfig;

@Slf4j
public class StubGeminiClient extends GeminiClient {

    public StubGeminiClient(GeminiConfig config) {
        super(config);
    }

    @Override
    @Async
    public CompletableFuture<String> generateContent(String prompt) {
        validatePrompt(prompt);

        log.debug("StubGeminiClient: Generating mock response for system test");

        String mockResponse = generateMockResponse();
        return CompletableFuture.completedFuture(mockResponse);
    }

    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }
    }

    private String generateMockResponse() {
        return """
                [
                  {
                    "transactionId": "00000000-0000-0000-0000-000000000001",
                    "categoryId": null,
                    "confidence": 0.8
                  }
                ]
                """;
    }
}
