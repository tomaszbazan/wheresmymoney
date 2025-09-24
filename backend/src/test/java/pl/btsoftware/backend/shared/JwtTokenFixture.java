package pl.btsoftware.backend.shared;

import org.jetbrains.annotations.NotNull;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public final class JwtTokenFixture {
    private JwtTokenFixture() {
    }

    public static @NotNull JwtRequestPostProcessor createTokenFor(String userId) {
        return jwt().jwt(jwt -> jwt.subject(userId));
    }
}
