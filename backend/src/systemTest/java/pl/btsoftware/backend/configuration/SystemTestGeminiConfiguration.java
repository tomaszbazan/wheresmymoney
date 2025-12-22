package pl.btsoftware.backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pl.btsoftware.backend.ai.infrastructure.client.GeminiClient;
import pl.btsoftware.backend.ai.infrastructure.config.GeminiConfig;

@Configuration
@Profile("system-test")
public class SystemTestGeminiConfiguration {

    @Bean
    @Primary
    public GeminiConfig geminiConfig() {
        return new StubGeminiConfig();
    }

    @Bean
    @Primary
    public GeminiClient geminiClient(GeminiConfig config) {
        return new StubGeminiClient(config);
    }
}
