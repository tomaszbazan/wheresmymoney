package pl.btsoftware.backend.ai.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@ConditionalOnProperty(name = "gemini.enabled", havingValue = "true", matchIfMissing = false)
@EnableRetry
@EnableAsync
public final class AsyncRetryConfig {
}
