package pl.btsoftware.backend.configuration;

import pl.btsoftware.backend.ai.infrastructure.config.GeminiConfig;

public class StubGeminiConfig extends GeminiConfig {

    static {
        System.setProperty("GEMINI_API_KEY", "stub-api-key-for-system-tests");
    }

    public StubGeminiConfig() {
        super();
    }
}
